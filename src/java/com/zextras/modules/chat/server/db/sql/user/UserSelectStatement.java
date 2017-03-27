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

package com.zextras.modules.chat.server.db.sql.user;

import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.sql.SqlParameter;
import com.zextras.modules.chat.server.db.sql.SqlStatement;

import java.util.ArrayList;
import java.util.Collection;

public class UserSelectStatement implements SqlStatement {
  
  private Collection<SqlParameter> mParameters = new ArrayList<SqlParameter>(1);
  private final String statement;
  
  public UserSelectStatement(final SpecificAddress address) {
    mParameters.add(new SqlParameter<String>(1, "address", address.toString()));
    statement = "SELECT * FROM chat.USER WHERE address=? LIMIT 1";
  }
  
  public UserSelectStatement(final int userId) {
    mParameters.add(new SqlParameter<Integer>(1, "userId", userId));
    statement = "SELECT * FROM chat.USER WHERE id=? LIMIT 1";
  }
  
  @Override
  public String sql() {
    return statement;
  }
  
  @Override
  public Collection<SqlParameter> parameters() {
    return mParameters;
  }
}
