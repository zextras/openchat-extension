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
import com.zextras.lib.db.DbHandler;
import com.zextras.modules.chat.server.db.sql.GenericStatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Singleton
public class BackupDbOperations
{
  private final DbHandler        mDbHandler;
  private final GenericStatement CHECKPOINT_STATEMENT = new GenericStatement("CHECKPOINT");

  @Inject
  public BackupDbOperations(ChatDbHandler dbHandler)
  {
    mDbHandler = dbHandler;
  }

  public void execBackup(String backupPath)
    throws SQLException
  {
    {
      java.io.File backupFile = new java.io.File(backupPath);

      if (!backupFile.getParentFile().exists())
      {
        backupFile.getParentFile().mkdirs();
      }

      backupFile.delete();

      Connection connection = mDbHandler.getConnection();

      PreparedStatement statement = connection.prepareStatement(CHECKPOINT_STATEMENT.sql());
      statement.execute();

      String backupSql = " BACKUP DATABASE TO '" + backupPath + "' NOT BLOCKING";
      GenericStatement backupStatement = new GenericStatement(backupSql);

      statement = connection.prepareStatement(backupStatement.sql());
      statement.execute();
    }
  }
}
