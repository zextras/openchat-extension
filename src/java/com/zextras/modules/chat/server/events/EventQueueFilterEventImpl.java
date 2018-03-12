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

package com.zextras.modules.chat.server.events;

import com.zextras.modules.chat.server.exceptions.ChatException;

public class EventQueueFilterEventImpl implements EventQueueFilterEvent
{
  @Override
  public Boolean interpret(final Event event)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventBootCompleted eventBootCompleted) throws ChatException
  {
    return false;
  }

  @Override
  public Boolean interpret(EventStatusProbe eventStatusProbe)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventStatuses eventStatuses)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventStatusChanged eventStatusChanged)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventSoapSessionRegistered eventSoapSessionRegistered)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventNewClientVersion eventNewClientVersion)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventMessageSizeExceeded eventMessageSizeExceeded)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventMessageBack eventMessageBack)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventMessageAck eventMessageAck)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventMessage eventMessage)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventIsWriting eventIsWriting)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventGetRelationships eventGetRelationships)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventGetPrivacy eventGetPrivacy)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventFriendRenamed eventFriendRenamed)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventFriendBackRemove eventFriendBackRemove)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventFriendBackAdded eventFriendBackAdded)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventFriendBackAccepted eventFriendBackAccepted)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventFriendAddedForClient eventFriendAddedForClient)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventFriendAdded eventFriendAdded)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventFriendAccepted eventFriendAccepted)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventBindResult eventBindResult)
  {
    return false;
  }

  @Override
  public Boolean interpret(FriendNotFoundEvent friendNotFoundEvent)
  {
    return false;
  }

  @Override
  public Boolean interpret(FeatureNotImplementedEvent featureNotImplementedEvent)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventStreamStarted eventXmppSessionRegistered)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventIQAuthResult eventIqAuthResult)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventXmppSASLAuthentication eventXmppSASLAuthentication)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventXmppSessionFeatures eventXmppSessionFeatures)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventXmppRedirect eventXmppRedirect)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventXmppSessionEstablished eventXmppSessionEstablished)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventXmppPing eventXmppPing)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventDiscovery event)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventFloodControl event)
  {
    return false;
  }

  @Override
  public Boolean interpret(EventIQQuery event) throws ChatException
  {
    return false;
  }

  @Override
  public Boolean interpret(EventMessageHistory event) throws ChatException
  {
    return false;
  }

  @Override
  public Boolean interpret(EventMessageHistoryLast event) throws ChatException
  {
    return false;
  }

  @Override
  public Boolean interpret(EventSharedFile event) throws ChatException
  {
    return false;
  }

}
