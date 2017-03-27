/*
 * ZAL - The abstraction layer for Zimbra.
 * Copyright (C) 2017 ZeXtras S.r.l.
 *
 * This file is part of ZAL.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZAL. If not, see <http://www.gnu.org/licenses/>.
 */

package com.zextras.modules.chat.server.operations;

import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.events.*;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.session.CommonSessionEventInterceptorBuilder;
import com.zextras.modules.chat.server.session.SessionManager;
import com.zextras.modules.chat.server.session.SessionUUID;
import com.zextras.modules.chat.server.User;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.xmpp.AuthStatus;
import com.zextras.modules.chat.server.xmpp.ConnectionType;
import com.zextras.modules.chat.server.xmpp.XmppAuthentication;
import com.zextras.modules.chat.server.xmpp.XmppError;
import com.zextras.modules.chat.server.xmpp.XmppEventFilter;
import com.zextras.modules.chat.server.xmpp.XmppFilterOut;
import com.zextras.modules.chat.server.xmpp.XmppSession;
import com.zextras.modules.chat.server.xmpp.netty.StanzaProcessor;
import com.zextras.modules.chat.server.xmpp.parsers.SASLAuthParser;
import org.openzal.zal.*;
import org.openzal.zal.AccountStatus;
import org.openzal.zal.exceptions.AuthFailedException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;


public class XmppSASLAuthentication implements ChatOperation
{
  private final CommonSessionEventInterceptorBuilder mCommonSessionEventInterceptorBuilder;
  private final Provisioning mProvisioning;
  private final SASLAuthParser                        mParser;
  private final StanzaProcessor.XmppConnectionHandler mXmppConnectionHandler;
  private final UserProvider                          mOpenUserProvider;
  private final XmppEventFilter mXmppEventFilter;
  private final XmppFilterOut mXmppFilterOut;
  private final XmppAuthentication                    mPlainAuthentication;
  private String mUsername   = "";
  private String mPassword   = "";
  private String mMechanism  = "";
  private String mRemoteHost = "";

  public XmppSASLAuthentication(
    CommonSessionEventInterceptorBuilder commonSessionEventInterceptorBuilder,
    Provisioning provisioning,
    SASLAuthParser parser,
    StanzaProcessor.XmppConnectionHandler xmppConnectionHandler,
    UserProvider openUserProvider,
    XmppEventFilter xmppEventFilter,
    XmppFilterOut xmppFilterOut
  )
  {
    mCommonSessionEventInterceptorBuilder = commonSessionEventInterceptorBuilder;
    mProvisioning = provisioning;
    mParser = parser;
    mXmppConnectionHandler = xmppConnectionHandler;
    mOpenUserProvider = openUserProvider;
    mXmppEventFilter = xmppEventFilter;
    mXmppFilterOut = xmppFilterOut;
    mPlainAuthentication = new XmppAuthentication("PLAIN");
  }

  @Override
  public List<Event> exec(SessionManager sessionManager, UserProvider userProvider)
    throws ChatException, ChatDbException
  {
    mMechanism = mParser.getMechanism();
    String encodeAuthString = mParser.getAuthString();

    parseAuthString(encodeAuthString, mXmppConnectionHandler.getSession());
    AuthStatus authStatus = authenticate(sessionManager);

    if (authStatus == AuthStatus.ACCOUNT_ON_OTHER_SERVER)
    {
      Account account = mProvisioning.getAccountByName(getUsername());
      mXmppConnectionHandler.transparentProxySASLAuthForAccount(account);

      return Collections.emptyList();
    }

    Event event = new EventXmppSASLAuthentication(
      mUsername,
      authStatus
    );

    mXmppConnectionHandler.getSession().getEventQueue().queueEvent(event);

    if (authStatus == AuthStatus.SUCCESS)
    {
      sessionManager.addSession(mXmppConnectionHandler.getSession());

      Event startStreamEvent = new EventStreamStarted(
        mXmppConnectionHandler.getSession().getId(),
        ConnectionType.Client,
        mXmppConnectionHandler.getSession().getDomain(),
        new HashSet<XmppError>()
      );
      mXmppConnectionHandler.getSession().getEventQueue().queueEvent(startStreamEvent);
    }

    return Collections.emptyList();
  }

  private AuthStatus authenticate( SessionManager sessionManager )
  {
    if (getPassword().isEmpty())
    {
      return AuthStatus.INVALID_CREDENTIALS;
    }

    if( !mXmppConnectionHandler.getSession().getAvailableAuthentications().contains(mPlainAuthentication) )
    {
      return AuthStatus.INVALID_CREDENTIALS;
    }

    try
    {
      Account account = mProvisioning.getAccountByName(getUsername());
      if (account == null)
      {
        return AuthStatus.INVALID_CREDENTIALS;
      }

      String accountStatus = account.getAccountStatus(mProvisioning);
      if (accountStatus.equals(AccountStatus.ACCOUNT_STATUS_LOCKED) ||
        accountStatus.equals(AccountStatus.ACCOUNT_STATUS_MAINTENANCE) ||
        accountStatus.equals(AccountStatus.ACCOUNT_STATUS_CLOSED))
      {
        return AuthStatus.ACCOUNT_DISABLED;
      }

      account.authAccount(getPassword(), Protocol.im);
      SpecificAddress specificAddress = new SpecificAddress(account.getName());

      if (!mProvisioning.onLocalServer(account)) {
        mRemoteHost = account.getMailHost();
        return AuthStatus.ACCOUNT_ON_OTHER_SERVER;
      }

      User user = mOpenUserProvider.getUser(specificAddress);
      boolean usingSSL = mXmppConnectionHandler.getSession().isUsingSSL();

      XmppSession xmppSession = new XmppSession(
        SessionUUID.randomUUID(),
        mXmppConnectionHandler.getSession().getEventQueue(),
        user,
        specificAddress,
        mXmppEventFilter,
        mXmppFilterOut
      );

      xmppSession.setDomain(mXmppConnectionHandler.getSession().getDomain());
      xmppSession.setUsingSSL(usingSSL);

      mXmppConnectionHandler.setSession( xmppSession, sessionManager );

      return AuthStatus.SUCCESS;

    }
    catch (AuthFailedException ex) {
      return AuthStatus.INVALID_CREDENTIALS;
    }
    catch (Exception ex) {
      ChatLog.log.warn(Utils.exceptionToString(ex));
      return AuthStatus.SERVER_ERROR;
    }
  }

  protected void parseAuthString(String encodeAuthString, XmppSession session) {
    byte[] auth;
    try
    {
      auth = Base64.decodeBase64(encodeAuthString.getBytes("UTF-8"));
    }
    catch (UnsupportedEncodingException e)
    {
      throw new RuntimeException(e);
    }
    String plainSaslAuth = new String(auth);
    plainSaslAuth = StringUtils.replace(plainSaslAuth, "\\40", "@");
    String[] tokens = StringUtils.split(plainSaslAuth, '\0');

    if( tokens.length < 3 )
    {
      mUsername = tokens[0];
      mPassword = tokens[1];
    }
    else
    {
      mUsername = tokens[1];
      mPassword = tokens[2];
    }

    if( !mUsername.contains("@") ){
      mUsername += '@'+session.getDomain();
    }
  }

  public String getUsername() {
    return mUsername;
  }

  public String getPassword() {
    return mPassword;
  }

  public String getMechanism() {
    return mMechanism;
  }

  public String getRemoteHost() {
    return mRemoteHost;
  }
}
