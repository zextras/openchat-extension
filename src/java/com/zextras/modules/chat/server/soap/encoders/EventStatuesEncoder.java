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

package com.zextras.modules.chat.server.soap.encoders;

import com.zextras.lib.json.JSONArray;
import com.zextras.lib.json.JSONObject;
import com.zextras.modules.chat.server.response.ChatSoapResponse;
import com.zextras.modules.chat.server.status.Status;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.client_contstants.ClientEventType;
import com.zextras.modules.chat.server.soap.SoapEncoder;
import com.zextras.modules.chat.server.events.EventStatuses;

public class EventStatuesEncoder implements SoapEncoder
{
  private final EventStatuses mEvent;

  public EventStatuesEncoder(EventStatuses event)
  {
    mEvent = event;
  }

  public void encode(ChatSoapResponse response, SpecificAddress target)
  {
    final JSONArray statuses = new JSONArray();

    for (Status status : mEvent.getStatusList())
    {
      final JSONObject statusJSON = new JSONObject();

      statusJSON.put("type", status.getType().toByte());
      statusJSON.put("message", status.getText());
      statusJSON.put("id", status.getId());
      //statusJSON.put("wasLast", status.wasLast());
      statuses.put(statusJSON);
    }

    final JSONObject message = new JSONObject();
    message.put("type", ClientEventType.USER_STATUSES);
    message.put("user_statuses", statuses);
    message.put("timestampSent", System.currentTimeMillis());

    response.addResponse(message);
  }
}
