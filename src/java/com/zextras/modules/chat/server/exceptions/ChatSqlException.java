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

package com.zextras.modules.chat.server.exceptions;

import com.zextras.lib.Error.ErrorCode;
import com.zextras.modules.chat.server.db.sql.SqlStatement;

public class ChatSqlException extends ChatDbException
{
  public ChatSqlException(SqlStatement sqlStatement)
  {
    this(sqlStatement.sql());
  }

  public ChatSqlException(String sql)
  {
    super(new ErrorCode()
    {
      public String getCodeString()
      {
        return "CHAT_SQL_EXCEPTION";
      }

      public String getMessage()
      {
        return "Unable to execute the SQL statement: {sqlStatement}";
      }
    });
    setDetail("sqlStatement", sql);
  }
}
