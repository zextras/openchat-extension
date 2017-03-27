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

package com.zextras.modules.chat.server.db.builders;

import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.providers.UserInfo;
import com.zextras.modules.chat.server.exceptions.ChatDbException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserInfoBuilder implements Builder<UserInfo>
{
  private ResultSet mRs;

  public UserInfoBuilder(ResultSet rs) {
    mRs = rs;
  }

  @Override
  public boolean next() throws ChatDbException {
    try
    {
      return mRs.next();
    }
    catch (SQLException e)
    {
      ChatDbException newEx = new ChatDbException(e.getMessage());
      newEx.initCause(e);
      throw newEx;
    }
  }

  public UserInfo build()
    throws ChatDbException {
    if (next())
    {
      try
      {
        int userId = mRs.getInt("id");
        SpecificAddress specificAddress = new SpecificAddress(mRs.getString("address"));
        return new UserInfo(userId, specificAddress);
      }
      catch (SQLException e)
      {
        ChatDbException newEx = new ChatDbException(e.getMessage());
        newEx.initCause(e);
        throw newEx;
      }
    }
    else
    {
      return new UserInfo();
    }
  }
}
