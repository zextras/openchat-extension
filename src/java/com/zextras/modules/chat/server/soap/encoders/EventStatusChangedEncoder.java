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

import com.zextras.lib.json.JSONArray;
import com.zextras.lib.json.JSONObject;
import com.zextras.modules.chat.server.response.ChatSoapResponse;
import com.zextras.modules.chat.server.status.Status;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.client_contstants.ClientEventType;
import com.zextras.modules.chat.server.soap.SoapEncoder;
import com.zextras.modules.chat.server.events.EventStatusChanged;

public class EventStatusChangedEncoder implements SoapEncoder
{
  private final EventStatusChanged mEvent;

  public EventStatusChangedEncoder(EventStatusChanged event)
  {
    mEvent = event;
  }

  public void encode(ChatSoapResponse response, SpecificAddress target)
  {
    Status statusToSet = mEvent.getStatus();

    final JSONObject message = new JSONObject();
    message.put("type", ClientEventType.CONTACT_INFORMATION);
    message.put("id", statusToSet.getId().id());

    message.put("statusType", statusToSet.getType());
    message.put("message", statusToSet.getText());
    message.put("from", mEvent.getSender().resourceAddress());
    message.put("timestampSent", System.currentTimeMillis());
    message.put("group", mEvent.getType().toString());

    message.put("validSince", mEvent.getStatus().validSince());
    JSONArray meetings = new JSONArray();
    for (SpecificAddress address:mEvent.getStatus().meetings())
    {
      meetings.put(address.withoutResource().toString());
    }
    message.put("meetings", meetings);

    response.addResponse(message);
  }
}
