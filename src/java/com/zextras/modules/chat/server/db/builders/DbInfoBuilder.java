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

package com.zextras.modules.chat.server.db.builders;

import com.zextras.modules.chat.server.db.providers.DbInfo;
import com.zextras.modules.chat.server.exceptions.ChatDbException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DbInfoBuilder implements Builder<DbInfo>
{
  private ResultSet mRs;

  public DbInfoBuilder(ResultSet rs) {
    mRs = rs;
  }

  @Override
  public boolean next()
    throws ChatDbException
  {
    try {
      return mRs.next();
    }
    catch (SQLException ex)
    {
      ChatDbException newEx = new ChatDbException(ex.getMessage());
      newEx.initCause(ex);
      throw newEx;
    }
  }

  public DbInfo build()
    throws ChatDbException
  {
    if (next())
    {
      try
      {
        int dbVersion = mRs.getInt("version");
        return new DbInfo(dbVersion);
      }
      catch (SQLException ex)
      {
        ChatDbException newEx = new ChatDbException(ex.getMessage());
        newEx.initCause(ex);
        throw newEx;
      }
    }
    else
    {
      return new DbInfo();
    }
  }
}
