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

import com.zextras.lib.json.JSONArray;
import com.zextras.lib.json.JSONException;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.exceptions.MissingParameterException;
import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.operations.SendMessageAck;
import com.zextras.modules.chat.server.session.SessionUUID;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SoapCommandMessageReceived extends SoapCommand
{
  private static final Pattern COMPILE = Pattern.compile(",");
  private JSONArray mMessageIds;

  public SoapCommandMessageReceived(
    SpecificAddress senderAddress,
    Map<String, String> parameters
  )
  {
    super(senderAddress,
          parameters
    );
    mMessageIds = null;
  }

  public JSONArray getMessageIds()
  {
    if (mMessageIds == null)
    {
      try
      {
        mMessageIds = JSONArray.fromString(mParameterMap.get("message_ids"));
      }
      catch (JSONException e)
      {
        mMessageIds = new JSONArray();
      }

      if (mMessageIds.isEmpty())
      {
        mMessageIds = new JSONArray(Arrays.asList(COMPILE.split(mParameterMap.get("message_ids"))));
      }
    }

    return mMessageIds;
  }

  @Override
  public List<ChatOperation> createOperationList() throws MissingParameterException
  {
    JSONArray message_ids = getMessageIds();

    SessionUUID sessionUUID = SessionUUID.fromString(mParameterMap.get(SESSION_ID));
    LinkedList<ChatOperation> operations = new LinkedList<ChatOperation>();

    for (int i = 0; i < message_ids.length(); i++)
    {
      String id = message_ids.getString(i);
      EventId message_id = EventId.fromString( id );
      operations.add(
        new SendMessageAck(
          mSenderAddress,
          getTargetAddress(),
          message_id,
          sessionUUID
        )
      );
    }

    return operations;
  }
}
