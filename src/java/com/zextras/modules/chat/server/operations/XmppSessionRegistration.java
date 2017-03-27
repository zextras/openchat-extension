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
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventQueue;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.session.SessionManager;

import com.zextras.modules.chat.server.events.EventStreamStarted;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.xmpp.ConnectionType;
import com.zextras.modules.chat.server.xmpp.StanzaWriterFactory;
import com.zextras.modules.chat.server.xmpp.netty.StanzaWriter;
import com.zextras.modules.chat.server.xmpp.XmppError;
import com.zextras.modules.chat.server.xmpp.netty.StanzaProcessor;
import org.openzal.zal.Domain;
import org.openzal.zal.Provisioning;
import org.openzal.zal.exceptions.ZimbraException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class XmppSessionRegistration implements ChatOperation
{
  private final Provisioning                          mProvisioning;
  private final StanzaProcessor.XmppConnectionHandler mXmppConnectionHandler;
  private final Set<XmppError> mXmppErrors = new HashSet<XmppError>();
  private final ConnectionType      mClientType;
  private final String              mDomain;
  private final StanzaWriterFactory mStanzaWriterFactory;

  public XmppSessionRegistration(
    Provisioning provisioning,
    StanzaProcessor.XmppConnectionHandler xmppConnectionHandler,
    ConnectionType clientType,
    String domain,
    StanzaWriterFactory stanzaWriterFactory
  )
  {
    mProvisioning = provisioning;
    mXmppConnectionHandler = xmppConnectionHandler;
    mClientType = clientType;
    mDomain = domain;
    mStanzaWriterFactory = stanzaWriterFactory;
  }

  @Override
  public List<Event> exec(SessionManager sessionManager, UserProvider userProvider)
    throws ChatException, ChatDbException
  {
    EventQueue eventQueue = mXmppConnectionHandler.getSession().getEventQueue();

    if (!checkDomain(mDomain))
    {
      ChatLog.log.warn("Unable to find domain: " + mDomain);
      mXmppErrors.add(XmppError.UnknownHost);
    }

    // The session is been already authenticated and needs to be bind.
    if (mXmppConnectionHandler.getSession().isBindable())
    {
      return Collections.emptyList();
    }

    if (mXmppConnectionHandler.isNew())
    {
      StanzaWriter stanzaWriter = mStanzaWriterFactory.create(mXmppConnectionHandler);
      eventQueue.addListener(stanzaWriter);
      mXmppConnectionHandler.noLongerNew();
    }

    if (!mXmppConnectionHandler.getSession().isProxy())
    {
      Event event = new EventStreamStarted(
        mXmppConnectionHandler.getSession().getId(),
        mClientType,
        mDomain,
        mXmppErrors
      );

      eventQueue.queueEvent(event);
    }

    return Collections.emptyList();
  }

  private boolean checkDomain(String domainName)
    throws ChatException
  {
    try
    {
      Domain domain = mProvisioning.getDomainByName(domainName);
      return domain != null;
    }
    catch (ZimbraException e)
    {
      ChatException newEx = new ChatException(e.getMessage());
      newEx.initCause(e);
      throw newEx;
    }
  }

  public Set<XmppError> errors()
  {
    return mXmppErrors;
  }
}
