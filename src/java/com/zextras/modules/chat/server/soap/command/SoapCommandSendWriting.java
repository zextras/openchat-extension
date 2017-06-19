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

import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.WritingState;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.operations.SendIsWriting;
import com.zextras.modules.chat.server.exceptions.InvalidParameterException;
import com.zextras.modules.chat.server.exceptions.MissingParameterException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SoapCommandSendWriting extends SoapCommand
{
  public SoapCommandSendWriting(
    SpecificAddress senderAddress,
    Map<String, String> parameters
  )
  {
    super(senderAddress,
          parameters
    );
  }

  @Override
  public List<ChatOperation> createOperationList()
    throws MissingParameterException, InvalidParameterException
  {
    final WritingState state;
    final String stateStr = mParameterMap.get(VALUE);
    try
    {
      state = WritingState.fromByte( Byte.valueOf(stateStr));
    }
    catch(NumberFormatException e)
    {
      throw new InvalidParameterException("Invalid value " + stateStr + " for parameter state: must be a number.");
    }

    final ChatOperation sendWritingEvent = new SendIsWriting(
      mSenderAddress,
      new Target(getTargetAddress()),
      state
    );

    return Arrays.<ChatOperation>asList(sendWritingEvent);
  }
}
