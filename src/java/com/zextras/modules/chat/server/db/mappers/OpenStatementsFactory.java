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
import com.zextras.modules.chat.server.Relationship;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.sql.DbInfoSelectStatement;
import com.zextras.modules.chat.server.db.sql.SqlStatement;
import com.zextras.modules.chat.server.db.sql.relationship.RelationshipDeleteStatement;
import com.zextras.modules.chat.server.db.sql.relationship.RelationshipInsertStatement;
import com.zextras.modules.chat.server.db.sql.relationship.RelationshipSelectStatement;
import com.zextras.modules.chat.server.db.sql.relationship.RelationshipUpdateStatement;
import com.zextras.modules.chat.server.db.sql.user.UserDeleteStatement;
import com.zextras.modules.chat.server.db.sql.user.UserInsertStatement;
import com.zextras.modules.chat.server.db.sql.user.UserSelectAllStatement;
import com.zextras.modules.chat.server.db.sql.user.UserSelectStatement;
import com.zextras.modules.chat.server.db.sql.user.UserUpdateStatement;

public class OpenStatementsFactory implements StatementsFactory
{
  private final UserSelectAllStatement mUserSelectAllStatement;

  @Inject
  public OpenStatementsFactory(UserSelectAllStatement userSelectAllStatement)
  {
    mUserSelectAllStatement = userSelectAllStatement;
  }

  @Override
  public SqlStatement buildSelectUser(SpecificAddress address)
  {
    return new UserSelectStatement(address);
  }
  
  @Override
  public SqlStatement buildSelectUser(int userId)
  {
    return new UserSelectStatement(userId);
  }

  @Override
  public SqlStatement buildInsertUser(SpecificAddress address)
  {
    return new UserInsertStatement(address);
  }

  @Override
  public SqlStatement buildUpdateUser(int userId, SpecificAddress address)
  {
    return new UserUpdateStatement(userId, address);
  }

  @Override
  public SqlStatement buildDeleteUser(int userId)
  {
    return new UserDeleteStatement(userId);
  }

  @Override
  public SqlStatement buildSelectAllUsers()
  {
    return mUserSelectAllStatement;
  }

  @Override
  public SqlStatement buildSelectRelationships(int userId)
  {
    return new RelationshipSelectStatement(userId);
  }

  @Override
  public SqlStatement buildInsertRelationship(
    int userId,
    Relationship.RelationshipType relationshipType,
    SpecificAddress buddyAddress,
    String buddyNickname,
    String group
  )
  {
    return new RelationshipInsertStatement(
      userId,
      relationshipType,
      buddyAddress,
      buddyNickname,
      group
    );
  }

  @Override
  public SqlStatement buildUpdateRelationship(
    int relationshipId,
    Relationship.RelationshipType relationshipType,
    SpecificAddress buddyAddress,
    String buddyNickname,
    String group
  )
  {
    return new RelationshipUpdateStatement(
      relationshipId,
      relationshipType,
      buddyAddress,
      buddyNickname,
      group
    );
  }

  @Override
  public SqlStatement buildDeleteRelationship(int userId, String buddyAddress)
  {
    return new RelationshipDeleteStatement(userId, buddyAddress);
  }

  @Override
  public SqlStatement buildDbInfo()
  {
    return new DbInfoSelectStatement();
  }
}
