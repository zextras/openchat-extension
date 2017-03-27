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

package com.zextras.modules.chat.server.soap.command;

import com.zextras.lib.json.JSONObject;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.exceptions.InvalidParameterException;
import com.zextras.modules.chat.server.exceptions.MissingParameterException;
import com.zextras.modules.chat.server.operations.SendMessage;
import com.zextras.modules.chat.server.response.ChatSoapResponse;
import com.zextras.modules.chat.server.soap.SoapEncoder;
import org.openzal.zal.soap.SoapResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SoapCommandSendMessage extends SoapCommand
{
  private final SoapResponse mSoapResponse;

  public SoapCommandSendMessage(
    SoapResponse soapResponse,
    SpecificAddress senderAddress,
    Map<String, String> parameters
  )
  {
    super(senderAddress, parameters);
    mSoapResponse = soapResponse;
  }

  @Override
  public List<ChatOperation> createOperationList()
    throws MissingParameterException, InvalidParameterException
  {
    final String message = mParameterMap.get(MESSAGE);

    if (message == null)
    {
      throw new MissingParameterException("Missing parameters to create " + getClass().getName());
    }

    final EventId messageId = EventId.randomUUID();

    ChatOperation sendMessage = new SendMessage(
      messageId,
      mSenderAddress,
      getTargetAddress(),
      message
    );

    ChatSoapResponse response = new ChatSoapResponse();
    SoapEncoder soapEncoder = new SoapEncoder() {
      @Override
      public void encode(ChatSoapResponse response, SpecificAddress target) {
        JSONObject message = new JSONObject();
        message.put("message_id",messageId.toString());
        response.addResponse(message);
      }
    };
    soapEncoder.encode(response,mSenderAddress);
    response.encodeInSoapResponse(mSoapResponse);

    return Arrays.<ChatOperation>asList(sendMessage);
  }
}
