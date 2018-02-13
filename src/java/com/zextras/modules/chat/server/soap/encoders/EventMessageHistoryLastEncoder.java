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
import com.zextras.modules.chat.server.events.EventMessageHistory;
import com.zextras.modules.chat.server.events.EventMessageHistoryLast;
import com.zextras.modules.chat.server.response.ChatSoapResponse;
import com.zextras.modules.chat.server.soap.SoapEncoder;

public class EventMessageHistoryLastEncoder implements SoapEncoder
{
  private final EventMessageHistoryLast mEventHistoryLast;

  public EventMessageHistoryLastEncoder(EventMessageHistoryLast eventHistoryLast) {

    mEventHistoryLast = eventHistoryLast;
  }

  @Override
  public void encode(ChatSoapResponse response, SpecificAddress target)
  {
    final JSONObject message = new JSONObject();

    message.put("from",  mEventHistoryLast.getSender().toString());
    message.put("to", target.toString());
    message.put("query_id", mEventHistoryLast.getQueryId());
    message.put("first_id", mEventHistoryLast.getFirstId());
    message.put("last_id",  mEventHistoryLast.getLastId());

    response.addResponse(message);
  }
}
