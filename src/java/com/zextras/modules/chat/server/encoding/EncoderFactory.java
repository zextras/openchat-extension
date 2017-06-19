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

package com.zextras.modules.chat.server.encoding;

import com.zextras.modules.chat.server.events.*;

public interface EncoderFactory
{
  Encoder createEncoder(EventIsWriting eventIsWriting);
  Encoder createEncoder(EventFriendRenamed eventFriendRenamed);
  Encoder createEncoder(EventBindResult eventBindResult);
  Encoder createEncoder(EventStatuses eventStatuses);
  Encoder createEncoder(EventGetRelationships eventGetRelationships);
  Encoder createEncoder(EventStatusChanged eventStatusChanged);
  Encoder createEncoder(EventFriendAdded eventFriendAdded);
  Encoder createEncoder(EventFriendBackAdded eventBackFriendAdded);
  Encoder createEncoder(EventMessage eventMessage);
  Encoder createEncoder(EventSoapSessionRegistered eventSoapSessionRegistered);
  Encoder createEncoder(EventFriendAccepted eventFriendAccepted);
  Encoder createEncoder(EventFriendBackAccepted eventFriendBackAccepted);
  Encoder createEncoder(EventMessageAck eventMessageAck);
  Encoder createEncoder(EventStatusProbe eventStatusProbe);
  Encoder createEncoder(EventGetPrivacy eventGetPrivacy);
  Encoder createEncoder(EventMessageBack eventMessage);
  Encoder createEncoder(EventFriendBackRemove eventFriendBackRemove);
  Encoder createEncoder(EventNewClientVersion eventNewClientVersion);
  Encoder createEncoder(EventMessageSizeExceeded eventMessageSizeExceeded);
  Encoder createEncoder(FriendNotFoundEvent friendNotFoundEvent);
}
