/*
 * Copyright (C) 2017 ZeXtras S.r.l.
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
 * You should have received a copy of the GNU General Public License.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.zextras.modules.chat.server.operations;

import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.session.SessionManager;
import com.zextras.modules.chat.server.session.SessionUUID;
import com.zextras.modules.chat.server.User;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.EventIQAuthResult;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.xmpp.AuthStatus;
import com.zextras.modules.chat.server.xmpp.XmppAuthentication;
import com.zextras.modules.chat.server.xmpp.XmppEventFilter;
import com.zextras.modules.chat.server.xmpp.XmppFilterOut;
import com.zextras.modules.chat.server.xmpp.XmppSession;
import com.zextras.modules.chat.server.xmpp.netty.StanzaProcessor;
import com.zextras.modules.chat.server.xmpp.parsers.IQAuthXmppParser;
import org.openzal.zal.Protocol;
import org.openzal.zal.Provisioning;
import org.openzal.zal.Utils;
import org.openzal.zal.Account;
import org.openzal.zal.exceptions.ZimbraException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class XmppIQAuthentication implements ChatOperation
{
  private final Provisioning                          mProvisioning;
  private final IQAuthXmppParser mParser;
  private final StanzaProcessor.XmppConnectionHandler mXmppConnectionHandler;
  private final XmppFilterOut                         mXmppFilterOut;
  private final XmppEventFilter                       mXmppEventFilter;
  private final XmppAuthentication                    mPlainAuthentication;

  public XmppIQAuthentication(
    Provisioning provisioning,
    IQAuthXmppParser parser,
    StanzaProcessor.XmppConnectionHandler xmppConnectionHandler,
    XmppFilterOut xmppFilterOut,
    XmppEventFilter xmppEventFilter
  )
  {
    mProvisioning = provisioning;
    mParser = parser;
    mXmppConnectionHandler = xmppConnectionHandler;
    mXmppFilterOut = xmppFilterOut;
    mXmppEventFilter = xmppEventFilter;
    mPlainAuthentication = new XmppAuthentication("PLAIN");
  }

  @Override
  public List<Event> exec(SessionManager sessionManager, UserProvider userProvider)
    throws ChatException, ChatDbException
  {
    String domain = mParser.getDomain();
    if (domain.isEmpty())
    {
      domain = mXmppConnectionHandler.getSession().getDomain();
    }

    String resource = mParser.getResource();
    if (resource.isEmpty())
    {
      resource = UUID.randomUUID().toString();
    }

    String email = mParser.getUsername();

    if (!email.contains("@"))
    {
      email += "@" + domain;
    }

    AuthStatus mAuthStatus = AuthStatus.NOT_REQUESTED;

    if (!mParser.getPassword().isEmpty())
    {
      Collection<XmppAuthentication> availableAuthentications = mXmppConnectionHandler.getSession()
                                                                                      .getAvailableAuthentications();
      if (availableAuthentications.contains(mPlainAuthentication))
      {
        try
        {
          Account account = mProvisioning.getAccountByName(email);
          if (account != null)
          {
            account.authAccount(mParser.getPassword(), Protocol.im);

            if (!mProvisioning.onLocalServer(account))
            {
              mXmppConnectionHandler.transparentProxyIQAuthForAccount(account, mParser.getEventId(), mParser.getResource());

              return Collections.emptyList();
            }
            else
            {
              SpecificAddress specificAddress = new SpecificAddress(account.getName(), resource);
              User user = userProvider.getUser(specificAddress);

              boolean usingSSL = mXmppConnectionHandler.getSession().isUsingSSL();

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
              mAuthStatus = AuthStatus.SUCCESS;
            }
          }
          else
          {
            mAuthStatus = AuthStatus.INVALID_CREDENTIALS;
          }
        }
        catch (ZimbraException ex)
        {
          mAuthStatus = AuthStatus.INVALID_CREDENTIALS;
        }
        catch (Exception ex)
        {
          ChatLog.log.warn(Utils.exceptionToString(ex));
          mAuthStatus = AuthStatus.SERVER_ERROR;
        }
      }
      else
      {
        mAuthStatus = AuthStatus.INVALID_CREDENTIALS;
      }
    }

    Event event = new EventIQAuthResult(
      EventId.fromString(mParser.getEventId()),
      mAuthStatus,
      mXmppConnectionHandler.getSession().getAvailableAuthentications()
    );

    mXmppConnectionHandler.getSession().getEventQueue().queueEvent(event);

    return Collections.emptyList();
  }
}
