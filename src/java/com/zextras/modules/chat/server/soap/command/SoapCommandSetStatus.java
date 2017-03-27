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

import com.zextras.modules.chat.server.session.SessionUUID;
import com.zextras.modules.chat.server.status.StatusId;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.operations.SetStatus;
import com.zextras.modules.chat.server.exceptions.InvalidParameterException;
import com.zextras.modules.chat.server.exceptions.MissingParameterException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SoapCommandSetStatus extends SoapCommand
{
  public SoapCommandSetStatus(
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
    final SessionUUID sessionId = SessionUUID.fromString(mParameterMap.get(SESSION_ID));
    final String status = mParameterMap.get(STATUS_ID);
    final StatusId statusId;

    try
    {
      statusId = new StatusId( Integer.parseInt(status) );
    }
    catch(NumberFormatException e)
    {
      throw new InvalidParameterException("Invalid value " + status + " for parameter state: must be a number.");
    }

    final ChatOperation setStatus = new SetStatus(
      sessionId,
      statusId
    );

    return Arrays.<ChatOperation>asList(setStatus);
  }
}
