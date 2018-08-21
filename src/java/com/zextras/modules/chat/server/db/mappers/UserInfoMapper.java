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
import com.google.inject.Singleton;

import com.zextras.lib.db.DbHandler;
import com.zextras.modules.chat.server.db.ChatDbHandler;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.builders.UserInfoBuilder;
import com.zextras.modules.chat.server.db.providers.UserInfo;

import java.sql.ResultSet;

@Singleton
public class UserInfoMapper extends AbstractMapper<UserInfo> {

  private final StatementsFactory mStatementsFactory;

  @Inject
  public UserInfoMapper(
    ChatDbHandler dbHandler,
    StatementsFactory statementsFactory
  )
  {
    super(dbHandler);
    mStatementsFactory = statementsFactory;
  }

  public UserInfo get(SpecificAddress address)
    throws ChatDbException
  {
    return abstractFind(mStatementsFactory.buildSelectUser(address));
  }
  
  public UserInfo get(int userId)
    throws ChatDbException
  {
    return abstractFind(mStatementsFactory.buildSelectUser(userId));
  }

  public int insert(SpecificAddress address)
    throws ChatDbException
  {
    return abstractExecute(mStatementsFactory.buildInsertUser(address));
  }

  public int update(int userId, SpecificAddress address)
    throws ChatDbException
  {
    return abstractExecute(mStatementsFactory.buildUpdateUser(userId, address));
  }

  public int delete(int userId)
    throws ChatDbException
  {
    return abstractExecute(mStatementsFactory.buildDeleteUser(userId));
  }

  @Override
  protected UserInfo load(ResultSet rs)
    throws ChatDbException
  {
    return new UserInfoBuilder(rs).build();
  }
}
