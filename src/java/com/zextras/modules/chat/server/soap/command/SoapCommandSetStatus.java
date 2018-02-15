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

import com.zextras.lib.json.JSONArray;
import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.session.SessionUUID;
import com.zextras.modules.chat.server.status.Status;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.operations.SetStatus;
import com.zextras.modules.chat.server.exceptions.InvalidParameterException;
import com.zextras.modules.chat.server.exceptions.MissingParameterException;
import com.zextras.modules.chat.server.status.VolatileStatus;
import org.openzal.zal.Utils;
import org.openzal.zal.lib.Clock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SoapCommandSetStatus extends SoapCommand
{
  private final Clock mClock;

  public SoapCommandSetStatus(
    SpecificAddress senderAddress,
    Map<String, String> parameters,
    Clock clock
  )
  {
    super(senderAddress,
          parameters
    );
    mClock = clock;
  }

  @Override
  public List<ChatOperation> createOperationList()
    throws MissingParameterException, InvalidParameterException
  {
    final SessionUUID sessionId = SessionUUID.fromString(mParameterMap.get(SESSION_ID));
    final String status = mParameterMap.get(STATUS_ID);
    Status.StatusType statusType;

    try
    {
     statusType = Status.StatusType.fromByte(
        Byte.valueOf(status)
      );
    }
    catch(NumberFormatException e)
    {
      throw new InvalidParameterException("Invalid value " + status + " for parameter state: must be a number.");
    }

    JSONArray rawMeetings;
    try
    {
      rawMeetings = JSONArray.fromString(mParameterMap.get(SoapCommand.MEETINGS));
    }
    catch (Exception e)
    {
      ChatLog.log.err(Utils.exceptionToString(e));
      return Collections.emptyList();
    }

    List<SpecificAddress> meetings;
    if( rawMeetings.isEmpty() )
    {
      meetings = Collections.emptyList();
    }
    else
    {
      meetings = new ArrayList<>(rawMeetings.length());
      for (Object rawMeeting : rawMeetings) {
        meetings.add(new SpecificAddress(rawMeeting.toString()));
      }
    }

    final ChatOperation setStatus = new SetStatus(
      sessionId,
      new VolatileStatus(
        statusType,
        "",
        mClock.now(),
        meetings
      )
    );

    return Arrays.<ChatOperation>asList(setStatus);
  }
}
