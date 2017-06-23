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

package com.zextras.modules.chat.server.soap.encoders;


import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.EventFriendBackAccepted;
import com.zextras.modules.chat.server.response.ChatSoapResponse;
import com.zextras.modules.chat.server.soap.SoapEncoder;

public class EventFriendBackAcceptedEncoder implements SoapEncoder
{
  private EventFriendBackAccepted mEvent;

  public EventFriendBackAcceptedEncoder(EventFriendBackAccepted eventFriendBackAccepted) {
    mEvent = eventFriendBackAccepted;
  }

  public void encode(ChatSoapResponse response, SpecificAddress target)
  {
    /*final String senderString = mEvent.getSender().toString();
    final JSONObject message = new JSONObject();
    message.put("type", ClientEventType.FRIEND_REQUEST);
    message.put("statusType", FriendshipEventType.FRIENDSHIP_ACCEPTED);
    message.put("from", senderString);
    message.put("to", target.toString());
    message.put("timestampSent", System.currentTimeMillis());

    response.addResponse(message);  */
  }
}