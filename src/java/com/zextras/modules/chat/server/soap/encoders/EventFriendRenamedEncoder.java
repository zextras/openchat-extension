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

import com.zextras.lib.json.JSONObject;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.client_contstants.ClientEventType;
import com.zextras.modules.chat.server.client_contstants.FriendshipEventType;
import com.zextras.modules.chat.server.response.ChatSoapResponse;
import com.zextras.modules.chat.server.soap.SoapEncoder;
import com.zextras.modules.chat.server.events.EventFriendRenamed;

public class EventFriendRenamedEncoder implements SoapEncoder
{
  private final EventFriendRenamed mEvent;

  public EventFriendRenamedEncoder(EventFriendRenamed event)
  {
    mEvent = event;
  }

  public void encode(ChatSoapResponse response, SpecificAddress target)
  {
    final JSONObject message = new JSONObject();
    message.put("from", mEvent.getSender().toString());
    message.put("to", target.toString());
    message.put("type", ClientEventType.FRIEND_REQUEST);
    message.put("statusType", FriendshipEventType.FRIENDSHIP_RENAME);
   // message.put("timestampSent", mCurrentMillis);
    message.put("buddyAddress", mEvent.getFriendToRename().toString());
    message.put("buddyNickname", mEvent.getNewNickname()); //so the user's other sessions know the buddy nickname
    message.put("buddyGroup", mEvent.getNewGroup());

    response.addResponse(message);
  }
}
