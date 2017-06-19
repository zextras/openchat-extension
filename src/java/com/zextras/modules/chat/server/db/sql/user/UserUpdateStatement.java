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

package com.zextras.modules.chat.server.db.sql.user;

import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.sql.SqlParameter;
import com.zextras.modules.chat.server.db.sql.SqlStatement;

import java.util.ArrayList;
import java.util.Collection;

public class UserUpdateStatement implements SqlStatement {

  private Collection<SqlParameter> mParameters = new ArrayList<SqlParameter>(2);

  public UserUpdateStatement(
    final int id,
    final SpecificAddress address
  )
  {
    mParameters.add(new SqlParameter<String>(1, "ADDRESS", address.toString()));
    mParameters.add(new SqlParameter<Integer>(2, "ID", id, SqlParameter.DO_NO_UPDATE));
  }

  String table()
  {
    return "";
  }

  @Override
  public String sql() {
    StringBuilder sb = new StringBuilder("UPDATE chat.USER SET ");
    for (SqlParameter parameter : mParameters) {
      if (parameter.isFieldToUpdate()) {
        sb.append(parameter.getColumnName()).append("=?,");
      }
    }
    sb.setLength(sb.length() - 1);
    sb.append(" WHERE ID=?;");
    return sb.toString();
  }

  @Override
  public Collection<SqlParameter> parameters() {
    return mParameters;
  }
}
