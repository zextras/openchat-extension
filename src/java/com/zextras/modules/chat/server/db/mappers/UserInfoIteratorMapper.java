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

import com.google.inject.Inject;
import com.zextras.lib.db.DbHandler;
import com.zextras.modules.chat.server.db.ChatDbHandler;
import com.zextras.modules.chat.server.db.builders.UserInfoBuilder;
import com.zextras.modules.chat.server.db.providers.UserInfo;
import com.zextras.modules.chat.server.db.sql.SqlStatement;
import com.zextras.modules.chat.server.exceptions.ChatDbException;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserInfoIteratorMapper extends AbstractMapper<List<UserInfo>> {

  private final SqlStatement mUserSelectAllStatement;

  @Inject
  public UserInfoIteratorMapper(
    ChatDbHandler dbHandler,
    StatementsFactory statementsFactory
  )
  {
    super(dbHandler);
    mUserSelectAllStatement = statementsFactory.buildSelectAllUsers();
  }

  public List<UserInfo> get()
    throws ChatDbException
  {
    return abstractFind(mUserSelectAllStatement);
  }

  @Override
  protected List<UserInfo> load(ResultSet rs)
    throws ChatDbException
  {
    UserInfoBuilder builder = new UserInfoBuilder(rs);
    ArrayList<UserInfo> arrayList = new ArrayList<UserInfo>(1024);

    while( true )
    {
      UserInfo userInfo = builder.build();
      if( userInfo.isValid() )
      {
        arrayList.add(userInfo);
      }
      else
      {
        break;
      }
    }

    return arrayList;
  }
}
