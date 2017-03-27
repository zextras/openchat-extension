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

package com.zextras.modules.chat.server.events;

public interface EventInterpreter<T>
{
  T interpret(EventStatusProbe eventStatusProbe);

  T interpret(EventStatuses eventStatuses);

  T interpret(EventStatusChanged eventStatusChanged);

  T interpret(EventSoapSessionRegistred eventSoapSessionRegistred);

  T interpret(EventNewClientVersion eventNewClientVersion);

  T interpret(EventMessageSizeExceeded eventMessageSizeExceeded);

  T interpret(EventMessageBack eventMessageBack);

  T interpret(EventMessageAck eventMessageAck);

  T interpret(EventMessage eventMessage);

  T interpret(EventIsWriting eventIsWriting);

  T interpret(EventGetRelationships eventGetRelationships);

  T interpret(EventGetPrivacy eventGetPrivacy);

  T interpret(EventFriendRenamed eventFriendRenamed);

  T interpret(EventFriendBackRemove eventFriendBackRemove);

  T interpret(EventFriendBackAdded eventFriendBackAdded);

  T interpret(EventFriendBackAccepted eventFriendBackAccepted);

  T interpret(EventFriendAddedForClient eventFriendAddedForClient);

  T interpret(EventFriendAdded eventFriendAdded);

  T interpret(EventFriendAccepted eventFriendAccepted);

  T interpret(EventBindResult eventBindResult);

  T interpret(FriendNotFoundEvent friendNotFoundEvent);

  T interpret(FeatureNotImplementedEvent featureNotImplementedEvent);

  T interpret(EventStreamStarted eventXmppSessionRegistered);

  T interpret(EventIQAuthResult eventIqAuthResult);

  T interpret(EventXmppSASLAuthentication eventXmppSASLAuthentication);

  T interpret(EventXmppSessionFeatures eventXmppSessionFeatures);

  T interpret(EventXmppRedirect eventXmppRedirect);

  T interpret(EventXmppSessionEstablished eventXmppSessionEstablished);

  T interpret(EventXmppPing eventXmppPing);

  T interpret(EventXmppDiscovery event);

}
