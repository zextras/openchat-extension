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

package com.zextras.modules.chat.server.interceptors;

import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventBindResult;
import com.zextras.modules.chat.server.events.EventFloodControl;
import com.zextras.modules.chat.server.events.EventFriendAccepted;
import com.zextras.modules.chat.server.events.EventFriendAdded;
import com.zextras.modules.chat.server.events.EventFriendAddedForClient;
import com.zextras.modules.chat.server.events.EventFriendBackAccepted;
import com.zextras.modules.chat.server.events.EventFriendBackAdded;
import com.zextras.modules.chat.server.events.EventFriendBackRemove;
import com.zextras.modules.chat.server.events.EventFriendRenamed;
import com.zextras.modules.chat.server.events.EventGetPrivacy;
import com.zextras.modules.chat.server.events.EventGetRelationships;
import com.zextras.modules.chat.server.events.EventIQAuthResult;
import com.zextras.modules.chat.server.events.EventIQQuery;
import com.zextras.modules.chat.server.events.EventInterceptorFactory;
import com.zextras.modules.chat.server.events.EventIsWriting;
import com.zextras.modules.chat.server.events.EventMessage;
import com.zextras.modules.chat.server.events.EventMessageAck;
import com.zextras.modules.chat.server.events.EventMessageBack;
import com.zextras.modules.chat.server.events.EventMessageHistory;
import com.zextras.modules.chat.server.events.EventMessageHistoryLast;
import com.zextras.modules.chat.server.events.EventMessageSizeExceeded;
import com.zextras.modules.chat.server.events.EventNewClientVersion;
import com.zextras.modules.chat.server.events.EventSoapSessionRegistered;
import com.zextras.modules.chat.server.events.EventStatusChanged;
import com.zextras.modules.chat.server.events.EventStatusProbe;
import com.zextras.modules.chat.server.events.EventStatuses;
import com.zextras.modules.chat.server.events.EventStreamStarted;
import com.zextras.modules.chat.server.events.EventXmppDiscovery;
import com.zextras.modules.chat.server.events.EventXmppPing;
import com.zextras.modules.chat.server.events.EventXmppRedirect;
import com.zextras.modules.chat.server.events.EventXmppSASLAuthentication;
import com.zextras.modules.chat.server.events.EventXmppSessionEstablished;
import com.zextras.modules.chat.server.events.EventXmppSessionFeatures;
import com.zextras.modules.chat.server.events.FeatureNotImplementedEvent;
import com.zextras.modules.chat.server.events.FriendNotFoundEvent;
import com.zextras.modules.chat.server.exceptions.ChatException;

public class StubEventInterceptorFactory implements EventInterceptorFactory
{
  @Override
  public EventInterceptor interpret(Event event)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventBindResult eventBindResult)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventFriendAccepted eventFriendAccepted)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventFriendAddedForClient eventFriendAddedForClient)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventFriendAdded eventFriendAdded)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventFriendRenamed eventFriendRenamed)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventIsWriting eventIsWriting)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventMessage eventMessage)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventMessageBack eventMessage)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventGetRelationships eventGetRelationships)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventSoapSessionRegistered eventSoapSessionRegistered)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventStatusChanged eventStatusChanged)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventStatuses eventStatuses)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventMessageAck eventMessageAck)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventStatusProbe eventStatusProbe)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventGetPrivacy eventGetPrivacy)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventFriendBackAdded eventFriendBackAdded)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventFriendBackAccepted eventFriendBackAccepted)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventFriendBackRemove eventFriendBackRemove)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventNewClientVersion eventNewClientVersion)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventMessageSizeExceeded eventMessageSizeExceeded)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(FriendNotFoundEvent friendNotFoundEvent)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(FeatureNotImplementedEvent featureNotImplementedEvent)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventStreamStarted eventXmppSessionRegistered)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventIQAuthResult eventIqAuthResult)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventXmppSASLAuthentication eventXmppSASLAuthentication)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventXmppSessionFeatures eventXmppSessionFeatures)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventXmppRedirect eventXmppRedirect)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventXmppSessionEstablished eventXmppSessionEstablished)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventXmppPing eventXmppPing)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventXmppDiscovery event)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventFloodControl event)
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventIQQuery event) throws ChatException
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventMessageHistory event) throws ChatException
  {
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventMessageHistoryLast event) throws ChatException
  {
    return new StubEventInterceptor();
  }

}
