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

package com.zextras.modules.chat.server;

import com.zextras.lib.db.DbHandler;

import java.sql.Connection;
import java.sql.SQLException;

public class DbHandlerStub implements DbHandler
{
  @Override
  public Connection getConnection() throws SQLException
  {
    throw new RuntimeException();
  }

  @Override
  public String cleanSql(String sql)
  {
    return sql;
  }

  @Override
  public String getClusterSchemaName()
  {
    return "";
  }

  @Override
  public void start() throws ServiceStartException
  {}

  @Override
  public void stop()
  {}
}
