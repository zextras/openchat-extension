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

package com.zextras.modules.chat.server.db;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zextras.modules.chat.server.db.mappers.DbInfoMapper;
import com.zextras.modules.chat.server.db.providers.DbInfo;
import com.zextras.modules.chat.server.db.sql.DbInfoSelectStatement;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import org.openzal.zal.ZimbraConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;

@Singleton
public class MariaDbHandler implements DbHandler
{
  private final ZimbraConnectionProvider mZimbraConnectionProvider;

  @Inject
  public MariaDbHandler(
    ZimbraConnectionProvider zimbraConnectionProvider
  )
  {
    mZimbraConnectionProvider = zimbraConnectionProvider;
  }

  @Override
  public void shutdown()
  {}

  @Override
  public Connection getConnection() throws SQLException
  {
    return mZimbraConnectionProvider.getConnection().getConnection();
  }

  @Override
  public String cleanSql(String sql)
  {
    return sql;
  }

}
