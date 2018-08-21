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

package com.zextras.modules.chat.server.db.sql;

import com.zextras.lib.db.DbHandler;
import com.zextras.modules.chat.server.exceptions.ChatDbException;

import java.sql.Connection;
import java.sql.SQLException;


public abstract class SqlClosure<T> {
  private final DbHandler mDbHandler;

  public SqlClosure(DbHandler dbHandler)
  {
    mDbHandler = dbHandler;
  }

  public final T execute() throws ChatDbException
  {
    try
    {
      Connection connection = mDbHandler.getConnection();
      connection.setAutoCommit(false);
      try
      {
        return execute(connection);
      } finally {
        connection.commit();
        connection.close();
      }
    } catch (SQLException ex)
    {
      ChatDbException newEx = new ChatDbException("Error executing SQL Statement.");
      newEx.initCause(ex);
      throw newEx;
    }
  }

  public abstract T execute(final Connection connection) throws ChatDbException;
}
