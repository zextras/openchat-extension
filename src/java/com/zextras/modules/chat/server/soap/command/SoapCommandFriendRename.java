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
import com.zextras.modules.chat.server.operations.UpsertFriend;
import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.exceptions.InvalidParameterException;
import com.zextras.modules.chat.server.exceptions.MissingParameterException;
import org.openzal.zal.Provisioning;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SoapCommandFriendRename extends SoapCommand
{
  private final Provisioning mProvisioning;

  public SoapCommandFriendRename(
    SpecificAddress senderAddress,
    Map<String, String> parameters,
    Provisioning provisioning
  )
  {
    super(senderAddress, parameters);
    mProvisioning = provisioning;
  }

  @Override
  public List<ChatOperation> createOperationList()
    throws MissingParameterException, InvalidParameterException
  {
    final String targetNickName = mParameterMap.get(TARGET_USERNAME);
    String targetGroup = mParameterMap.get(TARGET_GROUP);

    if (targetGroup == null)
    {
      targetGroup = "";
    }

    final ChatOperation renameFriend = new UpsertFriend(
      mSenderAddress,
      getTargetAddress(),
      targetNickName,
      targetGroup,
      mProvisioning
    );

    return Arrays.<ChatOperation>asList(renameFriend);
  }
}
