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

public interface EventInterpreter<T>
{
  T interpret(Event event) throws ChatException;
  T interpret(EventBootCompleted eventBootCompleted) throws ChatException;
  T interpret(EventStatusProbe eventStatusProbe) throws ChatException;
  T interpret(EventStatuses eventStatuses) throws ChatException;
  T interpret(EventStatusChanged eventStatusChanged) throws ChatException;
  T interpret(EventSoapSessionRegistered eventSoapSessionRegistered) throws ChatException;
  T interpret(EventNewClientVersion eventNewClientVersion) throws ChatException;
  T interpret(EventMessageSizeExceeded eventMessageSizeExceeded) throws ChatException;
  T interpret(EventMessageBack eventMessageBack) throws ChatException;
  T interpret(EventMessageAck eventMessageAck) throws ChatException;
  T interpret(EventMessage eventMessage) throws ChatException;
  T interpret(EventIsWriting eventIsWriting) throws ChatException;
  T interpret(EventGetRelationships eventGetRelationships) throws ChatException;
  T interpret(EventGetPrivacy eventGetPrivacy) throws ChatException;
  T interpret(EventFriendRenamed eventFriendRenamed) throws ChatException;
  T interpret(EventFriendBackRemove eventFriendBackRemove) throws ChatException;
  T interpret(EventFriendBackAdded eventFriendBackAdded) throws ChatException;
  T interpret(EventFriendBackAccepted eventFriendBackAccepted) throws ChatException;
  T interpret(EventFriendAddedForClient eventFriendAddedForClient) throws ChatException;
  T interpret(EventFriendAdded eventFriendAdded) throws ChatException;
  T interpret(EventFriendAccepted eventFriendAccepted) throws ChatException;
  T interpret(EventBindResult eventBindResult) throws ChatException;
  T interpret(FriendNotFoundEvent friendNotFoundEvent) throws ChatException;
  T interpret(FeatureNotImplementedEvent featureNotImplementedEvent) throws ChatException;
  T interpret(EventStreamStarted eventXmppSessionRegistered) throws ChatException;
  T interpret(EventIQAuthResult eventIqAuthResult) throws ChatException;
  T interpret(EventXmppSASLAuthentication eventXmppSASLAuthentication) throws ChatException;
  T interpret(EventXmppSessionFeatures eventXmppSessionFeatures) throws ChatException;
  T interpret(EventXmppRedirect eventXmppRedirect) throws ChatException;
  T interpret(EventXmppSessionEstablished eventXmppSessionEstablished) throws ChatException;
  T interpret(EventXmppPing eventXmppPing) throws ChatException;
  T interpret(EventXmppDiscovery event) throws ChatException;
  T interpret(EventFloodControl event) throws ChatException;
}
