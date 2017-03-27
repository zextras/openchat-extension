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

package com.zextras.modules.chat.server.db.sql.relationship;

import com.zextras.modules.chat.server.db.sql.SqlParameter;
import com.zextras.modules.chat.server.db.sql.SqlStatement;

import java.util.ArrayList;
import java.util.Collection;

public class RelationshipSelectStatement implements SqlStatement {
  private Collection<SqlParameter> mParameters = new ArrayList<SqlParameter>(1);

  public RelationshipSelectStatement(final int userId) {
    mParameters.add(new SqlParameter<Integer>(1, "USERID", userId));
  }

  @Override
  public String sql() {
    return "SELECT * FROM chat.RELATIONSHIP WHERE USERID=?";
  }

  @Override
  public Collection<SqlParameter> parameters() {
    return mParameters;
  }
}
