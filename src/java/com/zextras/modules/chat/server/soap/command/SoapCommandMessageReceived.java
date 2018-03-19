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

package com.zextras.modules.chat.server.soap.command;

import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.exceptions.MissingParameterException;
import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.operations.SendMessageAck;
import com.zextras.modules.chat.server.session.SessionUUID;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SoapCommandMessageReceived extends SoapCommand
{
  public SoapCommandMessageReceived(
    SpecificAddress senderAddress,
    Map<String, String> parameters
  )
  {
    super(senderAddress,
          parameters
    );
  }

  // TODO: not more compatible with openchat or older talk
  @Override
  public List<ChatOperation> createOperationList() throws MissingParameterException
  {
    SessionUUID sessionUUID = SessionUUID.fromString(mParameterMap.get(SESSION_ID));
    String s = mParameterMap.get("message_date");
    long timestamp = System.currentTimeMillis();
    try
    {
      timestamp = Long.valueOf(s);
    }
    catch (NumberFormatException e)
    {}

    return Collections.<ChatOperation>singletonList(new SendMessageAck(
          mSenderAddress,
          new SpecificAddress(mParameterMap.get("target_address")),
          EventId.fromString(mParameterMap.get("message_id")),
          timestamp,
          sessionUUID)
        );
  }
}
