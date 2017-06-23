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

package com.zextras.modules.chat.server.db.mappers;

import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.db.DbHandler;
import com.zextras.modules.chat.server.db.sql.SqlClosure;
import com.zextras.modules.chat.server.db.sql.SqlParameter;
import com.zextras.modules.chat.server.db.sql.SqlStatement;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatSqlException;

import java.sql.*;


public abstract class AbstractMapper<T> {
  protected DbHandler mDbHandler;

  protected abstract T load(ResultSet rs) throws ChatDbException;

  public AbstractMapper(DbHandler dbHandler)
  {
    mDbHandler = dbHandler;
  }

  protected T abstractFind(final SqlStatement sqlStatement) throws ChatDbException
  {
    return new SqlClosure<T>(mDbHandler)
    {
      @Override
      public T execute(Connection connection)
        throws ChatDbException
      {
        PreparedStatement stmt = prepareStatement(connection, sqlStatement);
        try
        {
          ResultSet rs = stmt.executeQuery();
          return load(rs);
        } catch (SQLException e) {
          ChatSqlException sqlException = new ChatSqlException(sqlStatement);
          sqlException.initCause(e);
          throw sqlException;
        } finally {
          close(stmt);
        }
      }
    }.execute();
  }

  protected int abstractExecute(final SqlStatement sqlStatement) throws ChatDbException {
    return new SqlClosure<Integer>(mDbHandler) {
      @Override
      public Integer execute(Connection connection)
        throws ChatDbException
      {
        PreparedStatement stmt = prepareStatement(connection, sqlStatement);
        try
        {
          ChatLog.log.debug("SQL execute: "+sqlStatement.sql() +" " + sqlStatement.parameters().toString() );
          stmt.executeUpdate();
          ResultSet rs = stmt.getGeneratedKeys();

          int id = -1;
          if (rs.next()) {
            id = rs.getInt("id");
          }
          return id;
        } catch (SQLException e) {
          ChatSqlException sqlException = new ChatSqlException(sqlStatement);
          sqlException.initCause(e);
          throw sqlException;
        } finally {
          close(stmt);
        }
      }
    }.execute();
  }

  private void close(PreparedStatement stmt) {
    try {
      stmt.close();
    } catch (SQLException ignored) {}
  }

  private PreparedStatement prepareStatement(Connection connection,
                                             SqlStatement sqlStatement)
    throws ChatDbException
  {
    PreparedStatement stmt;
    try
    {
      stmt = connection.prepareStatement(sqlStatement.sql(),
                                                           Statement.RETURN_GENERATED_KEYS);
      for (SqlParameter parameter : sqlStatement.parameters())
      {
        stmt.setObject(parameter.getIndex(), parameter.getValue());
      }
    }
    catch (SQLException e)
    {
      ChatSqlException sqlException = new ChatSqlException(sqlStatement);
      sqlException.initCause(e);
      throw sqlException;
    }

    return stmt;
  }
}
