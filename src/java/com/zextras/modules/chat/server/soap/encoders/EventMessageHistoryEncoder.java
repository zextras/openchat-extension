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
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventMessageHistory;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.response.ChatSoapResponse;
import com.zextras.modules.chat.server.soap.SoapEncoder;

public class EventMessageHistoryEncoder implements SoapEncoder
{
  private final SoapEncoderFactory mSoapEncoderFactory;
  private final EventMessageHistory mEventHistory;

  public EventMessageHistoryEncoder(SoapEncoderFactory soapEncoderFactory, EventMessageHistory eventHistory) {
    mSoapEncoderFactory = soapEncoderFactory;

    mEventHistory = eventHistory;
  }

  @Override
  public void encode(ChatSoapResponse response, SpecificAddress target)
  {
    final JSONObject message = new JSONObject();
    Event originalEvent = mEventHistory.getOriginalMessage();
    SoapEncoder encoder;
    try
    {
      encoder = (SoapEncoder) originalEvent.interpret(mSoapEncoderFactory);
    }
    catch (ChatException e)
    {
      throw new UnsupportedOperationException("Unsupported history event " + originalEvent.getClass().getSimpleName());
    }

    ChatSoapResponse dummyResponse = new ChatSoapResponse();
    encoder.encode(dummyResponse,new SpecificAddress(mEventHistory.getOriginalMessage().getTarget().toSingleAddress()));

    message.put("type", ClientEventType.HISTORY);
    message.put("from",  mEventHistory.getSender().toString());
    message.put("to", target.toString());
    message.put("id", mEventHistory.getId().toString());
    message.put("query_id", mEventHistory.getQueryId());
    message.put("original_message", dummyResponse.getJson().getJSONObject(0));

    response.addResponse(message);
  }
}
