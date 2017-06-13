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
import com.zextras.modules.chat.server.User;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventIQAuthResult;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.events.EventStreamStarted;
import com.zextras.modules.chat.server.events.EventXmppSASLAuthentication;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.session.CommonSessionEventInterceptorBuilder;
import com.zextras.modules.chat.server.session.SessionManager;
import com.zextras.modules.chat.server.session.SessionUUID;
import com.zextras.modules.chat.server.xmpp.AuthStatus;
import com.zextras.modules.chat.server.xmpp.ConnectionType;
import com.zextras.modules.chat.server.xmpp.XmppError;
import com.zextras.modules.chat.server.xmpp.XmppEventFilter;
import com.zextras.modules.chat.server.xmpp.XmppFilterOut;
import com.zextras.modules.chat.server.xmpp.XmppSession;
import com.zextras.modules.chat.server.xmpp.netty.StanzaProcessor;
import com.zextras.modules.chat.server.xmpp.parsers.ProxyAuthParser;
import org.openzal.zal.Account;
import org.openzal.zal.AuthProvider;
import org.openzal.zal.Provisioning;

import java.util.Collections;
import java.util.List;

public class ProxyAuthentication implements ChatOperation
{
  private final Provisioning mProvisioning;
  private final CommonSessionEventInterceptorBuilder mCommonSessionEventInterceptorBuilder;
  private final AuthProvider mAuthProvider;
  private final StanzaProcessor.XmppConnectionHandler mXmppConnectionHandler;
  private final ProxyAuthParser mParser;
  private final XmppFilterOut mXmppFilterOut;
  private final XmppEventFilter mXmppEventFilter;

  public ProxyAuthentication(
    Provisioning provisioning,
    CommonSessionEventInterceptorBuilder commonSessionEventInterceptorBuilder,
    AuthProvider authProvider,
    StanzaProcessor.XmppConnectionHandler xmppConnectionHandler,
    ProxyAuthParser parser,
    XmppFilterOut xmppFilterOut,
    XmppEventFilter xmppEventFilter
  )
  {
    mProvisioning = provisioning;
    mCommonSessionEventInterceptorBuilder = commonSessionEventInterceptorBuilder;
    mAuthProvider = authProvider;
    mXmppConnectionHandler = xmppConnectionHandler;
    mParser = parser;
    mXmppFilterOut = xmppFilterOut;
    mXmppEventFilter = xmppEventFilter;
  }

  @Override
  public List<Event> exec(SessionManager sessionManager, UserProvider userProvider)
    throws ChatException, ChatDbException
  {
    AuthStatus authStatus;

    Account account = mProvisioning.getAccountById(mParser.getAuthToken().getAccountId());

    if (account == null)
    {
      ChatLog.log.err("Invalid proxy authentication: account not found (" + mParser.getAuthToken().getAccountId() + ")");
      authStatus = AuthStatus.INVALID_CREDENTIALS;
    }
    else if (!account.checkAuthTokenValidityValue(mParser.getAuthToken()))
    {
      ChatLog.log.err("Invalid proxy authentication: invalid AuthTokan (" + mParser.getAuthToken().toString() + ")");
      authStatus = AuthStatus.INVALID_CREDENTIALS;
    }
    else
    {
      SpecificAddress specificAddress = new SpecificAddress(account.getName(), mParser.getResource());
      User user = userProvider.getUser(specificAddress);

      boolean usingSSL = mParser.isUsingSSL();

      mXmppConnectionHandler.setSession(
        new XmppSession(
          SessionUUID.randomUUID(),
          mXmppConnectionHandler.getSession().getEventQueue(),
          mXmppConnectionHandler.getSocketChannel(),
          user,
          specificAddress,
          mXmppEventFilter,
          mXmppFilterOut
        ),
        sessionManager
      );

      mXmppConnectionHandler.getSession().setDomain(mXmppConnectionHandler.getSession().getDomain());
      mXmppConnectionHandler.getSession().setUsingSSL(usingSSL);

      sessionManager.addSession(mXmppConnectionHandler.getSession());
      authStatus = AuthStatus.SUCCESS;
    }

    Event event;
    switch (mParser.getAuthType())
    {
      case IQ:
        event = new EventIQAuthResult(
          EventId.fromString(mParser.getEventId()),
          authStatus,
          mXmppConnectionHandler.getSession().getAvailableAuthentications()
        );

        mXmppConnectionHandler.getSession().getEventQueue().queueEvent(event);
        break;

      case SASL:
        String username;
        if (account == null)
        {
          username = "unknown";
        }
        else
        {
          username = account.getName();
        }

        event = new EventXmppSASLAuthentication(
          username,
          authStatus
        );

        mXmppConnectionHandler.getSession().getEventQueue().queueEvent(event);
        mXmppConnectionHandler.getSession().getEventQueue().queueEvent(
          new EventStreamStarted(
            mXmppConnectionHandler.getSession().getId(),
            ConnectionType.Client,
            mXmppConnectionHandler.getSession().getDomain(),
            Collections.<XmppError>emptySet()
          )
        );
        break;

      default:
        throw new RuntimeException();
    }

    return Collections.emptyList();
  }

  public enum AuthType
  {
    IQ, SASL
  }
}
