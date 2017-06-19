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
import com.zextras.modules.chat.server.events.EventFriendBackRemove;
import com.zextras.modules.chat.server.response.ChatSoapResponse;
import com.zextras.modules.chat.server.soap.SoapEncoder;

public class EventFriendBackRemoveEncoder implements SoapEncoder
{
  private EventFriendBackRemove mEvent;

  public EventFriendBackRemoveEncoder(EventFriendBackRemove eventFriendBackRemove)
  {
    mEvent = eventFriendBackRemove;
  }

  public void encode(ChatSoapResponse response, SpecificAddress target)
  {
    final JSONObject message = new JSONObject();
    message.put("from", mEvent.getSender().toString());
    message.put("to", target.resourceAddress());
    message.put("type", ClientEventType.FRIEND_REQUEST );
    message.put("statusType", FriendshipEventType.FRIENDSHIP_DELETED);
    message.put("buddyAddress", mEvent.getFriendToRemove().toString());

    response.addResponse(message);
  }
}
