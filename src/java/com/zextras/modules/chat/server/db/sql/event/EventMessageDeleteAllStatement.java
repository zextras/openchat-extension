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

package com.zextras.modules.chat.server.db.sql.event;

import com.zextras.modules.chat.server.db.sql.SqlParameter;
import com.zextras.modules.chat.server.db.sql.SqlStatement;

import java.util.ArrayList;
import java.util.Collection;

public class EventMessageDeleteAllStatement implements SqlStatement {

  private Collection<SqlParameter> mParameters = new ArrayList<SqlParameter>();

  public EventMessageDeleteAllStatement(final int userId) {
    mParameters.add(new SqlParameter(1, "userId", userId));
  }

  @Override
  public String sql() {
    return "DELETE FROM chat.EVENTMESSAGE WHERE USERID=?";
  }

  @Override
  public Collection<SqlParameter> parameters() {
    return mParameters;
  }
}
