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

import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.exceptions.InvalidParameterException;
import com.zextras.modules.chat.server.exceptions.MissingParameterException;
import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.operations.RenameGroup;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class SoapCommandRenameGroup extends SoapCommand
{
  public SoapCommandRenameGroup(
    SpecificAddress senderAddress,
    Map<String, String> parameters
  )
  {
    super(senderAddress, parameters);
  }

  @Override
  public List<ChatOperation> createOperationList()
    throws MissingParameterException, InvalidParameterException
  {
    String currentGroupName = getTargetGroup();
    String newGroupName = mParameterMap.get(TARGET_NEW_GROUP);

    if (currentGroupName == null)
    {
      currentGroupName = "";
    }

    final ChatOperation renameGroup = new RenameGroup(
      getTargetAddress(),
      currentGroupName,
      newGroupName
    );

    return Arrays.asList(renameGroup);
  }
}
