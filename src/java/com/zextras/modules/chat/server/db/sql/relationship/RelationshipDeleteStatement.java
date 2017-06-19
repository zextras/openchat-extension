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

package com.zextras.modules.chat.server.db.sql.relationship;

import com.zextras.modules.chat.server.db.sql.SqlParameter;
import com.zextras.modules.chat.server.db.sql.SqlStatement;

import java.util.ArrayList;
import java.util.Collection;


public class RelationshipDeleteStatement implements SqlStatement {
  private final Collection<SqlParameter> mParameters = new ArrayList<SqlParameter>();

  public RelationshipDeleteStatement(int userId, String buddyAddress) {
    mParameters.add(new SqlParameter(1, "USERID", userId));
    mParameters.add(new SqlParameter(2, "BUDDYADDRESS", buddyAddress));
  }

  @Override
  public String sql() {
    return "DELETE FROM chat.RELATIONSHIP WHERE USERID=? AND BUDDYADDRESS=?";
  }

  @Override
  public Collection<SqlParameter> parameters() {
    return mParameters;
  }
}
