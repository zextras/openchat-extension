/*
 * Copyright (C) 2018 ZeXtras S.r.l.
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

import com.zextras.lib.Optional;
import com.zextras.lib.json.JSONObject;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.client_contstants.ClientEventType;
import com.zextras.modules.chat.server.events.EventLastMessageInfo;
import com.zextras.modules.chat.server.response.ChatSoapResponse;
import com.zextras.modules.chat.server.soap.SoapEncoder;
import org.apache.commons.lang3.tuple.Pair;

public class EventLastMessageInfoEncoder implements SoapEncoder
{
  private final EventLastMessageInfo mEvent;

  public EventLastMessageInfoEncoder(EventLastMessageInfo event)
  {
    mEvent = event;
  }

  @Override
  public void encode(ChatSoapResponse response, SpecificAddress target)
  {
    final JSONObject message = new JSONObject();
    message.put("type", ClientEventType.LAST_MESSAGE_INFO);
    message.put("from", mEvent.getSender().withoutResource().toString());
    message.put("to", target.toString());
    Optional<Pair<Long, String>> lastMessageSentInfo = mEvent.getLastSentMessageInfo();
    if (lastMessageSentInfo.hasValue())
    {
      JSONObject lastMessageSentInfoJson = new JSONObject();
      lastMessageSentInfoJson.put("id", lastMessageSentInfo.get().getRight());
      lastMessageSentInfoJson.put("date", lastMessageSentInfo.get().getLeft());
      message.put("last_message_sent", lastMessageSentInfoJson);
    }
    Optional<Pair<Long, String>> lastMessageReceivedInfo = mEvent.getLastIncomingMessageInfo();
    if (lastMessageReceivedInfo.hasValue())
    {
      JSONObject lastMessageReceivedInfoJson = new JSONObject();
      lastMessageReceivedInfoJson.put("id", lastMessageReceivedInfo.get().getRight());
      lastMessageReceivedInfoJson.put("date", lastMessageReceivedInfo.get().getLeft());
      message.put("last_message_received", lastMessageReceivedInfoJson);
    }
    if (mEvent.getUnreadCount().hasValue())
    {
      message.put("count", mEvent.getUnreadCount().get());
    }

    response.addResponse(message);
  }
}
