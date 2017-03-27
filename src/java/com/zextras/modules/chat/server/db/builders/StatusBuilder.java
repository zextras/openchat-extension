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

import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.status.CustomStatus;
import com.zextras.modules.chat.server.status.Status;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StatusBuilder {
  private ResultSet mRs;
  public static final int RESERVERED_IDS = 64;

  public StatusBuilder(ResultSet rs) {
    mRs = rs;
  }

  public boolean next()
    throws ChatDbException
  {
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

  public Status build()
    throws ChatDbException
  {
    try
    {
      int statusId = mRs.getInt("id") + RESERVERED_IDS;
      Status.StatusType statusType = Status.StatusType.fromByte(mRs.getByte("Type"));
      String statusText = mRs.getString("Text");
      return new CustomStatus(statusId, statusText, statusType);
    }
    catch (SQLException e)
    {
      ChatDbException newEx = new ChatDbException(e.getMessage());
      newEx.initCause(e);
      throw newEx;
    }
  }
}
