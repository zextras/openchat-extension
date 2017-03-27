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

import com.zextras.modules.chat.server.interceptors.EventInterceptor;
import com.zextras.modules.chat.server.interceptors.StubEventInterceptor;

public interface EventInterceptorFactory extends EventInterpreter<EventInterceptor>
{
  EventInterceptor interpret(EventBindResult eventBindResult);
  EventInterceptor interpret(EventFriendAccepted eventFriendAccepted);
  EventInterceptor interpret(EventFriendAddedForClient eventFriendAddedForClient);
  EventInterceptor interpret(EventFriendAdded eventFriendAdded);
  EventInterceptor interpret(EventFriendRenamed eventFriendRenamed);
  EventInterceptor interpret(EventIsWriting eventIsWriting);
  EventInterceptor interpret(EventMessage eventMessage);
  EventInterceptor interpret(EventMessageBack eventMessage);
  EventInterceptor interpret(EventGetRelationships eventGetRelationships);
  EventInterceptor interpret(EventSoapSessionRegistred eventSoapSessionRegistred);
  EventInterceptor interpret(EventStatusChanged eventStatusChanged);
  EventInterceptor interpret(EventStatuses eventStatuses);
  EventInterceptor interpret(EventMessageAck eventMessageAck);
  EventInterceptor interpret(EventStatusProbe eventStatusProbe);
  EventInterceptor interpret(EventGetPrivacy eventGetPrivacy);
  EventInterceptor interpret(EventFriendBackAdded eventFriendBackAdded);
  EventInterceptor interpret(EventFriendBackAccepted eventFriendBackAccepted);
  EventInterceptor interpret(EventFriendBackRemove eventFriendBackRemove);
  EventInterceptor interpret(EventNewClientVersion eventNewClientVersion);
  EventInterceptor interpret(EventMessageSizeExceeded eventMessageSizeExceeded);
  EventInterceptor interpret(FriendNotFoundEvent friendNotFoundEvent);
}
