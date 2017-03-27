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

package com.zextras.modules.chat.server.db.sql;

import java.util.ArrayList;
import java.util.Collection;

public abstract class GenericInsertStatement implements SqlStatement {
  private String mTableName;
  protected Collection<SqlParameter> mParameters;

  public GenericInsertStatement(String tableName)
  {
    mTableName = tableName;
    mParameters = new ArrayList<SqlParameter>();
  }

  @Override
  public String sql() {
    StringBuilder fieldsName = new StringBuilder().append(" (");
    StringBuilder valuesString = new StringBuilder().append(" VALUES (");

    for (SqlParameter sqlParameter : mParameters) {
      fieldsName.append(sqlParameter.getColumnName()).append(", ");
      valuesString.append("?, ");
    }
    fieldsName.setLength(fieldsName.length()-2);
    valuesString.setLength(valuesString.length()-2);
    fieldsName.append(")");
    valuesString.append(");");

    StringBuilder sb = new StringBuilder();
    sb.append("INSERT INTO ").append(mTableName).append(fieldsName).append(valuesString);
    return sb.toString();
  }

  @Override
  public Collection<SqlParameter> parameters() {
    return mParameters;
  }
}
