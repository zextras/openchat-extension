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

package com.zextras.modules.chat.server.db.mappers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zextras.modules.chat.server.Relationship;
import com.zextras.modules.chat.server.Relationship.RelationshipType;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.DbHandler;
import com.zextras.modules.chat.server.db.builders.RelationshipBuilder;
import com.zextras.modules.chat.server.exceptions.ChatDbException;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;

@Singleton
public class RelationshipMapper extends AbstractMapper<Collection<Relationship>> {

  private final StatementsFactory mStatementsFactory;

  @Inject
  public RelationshipMapper(
    DbHandler dbHandler,
    StatementsFactory statementsFactory
  )
  {
    super(dbHandler);
    mStatementsFactory = statementsFactory;
  }

  public Collection<Relationship> get(int userId)
    throws ChatDbException
  {
    return abstractFind(mStatementsFactory.buildSelectRelationships(userId));
  }

  public int insert(
    int userId,
    RelationshipType relationshipType,
    SpecificAddress buddyAddress,
    String buddyNickname,
    String group
  )
    throws ChatDbException
  {
    return abstractExecute(mStatementsFactory.buildInsertRelationship(
      userId,
      relationshipType,
      buddyAddress,
      buddyNickname,
      group
    ));
  }

  public int update(
    int relationshipId,
    RelationshipType relationshipType,
    SpecificAddress buddyAddress,
    String buddyNickname,
    String group
  )
      throws ChatDbException
  {
    return abstractExecute(mStatementsFactory.buildUpdateRelationship(
      relationshipId,
      relationshipType,
      buddyAddress,
      buddyNickname,
      group
    ));
  }

  public int delete(int userId, String buddyAddress)
    throws ChatDbException
  {
    return abstractExecute(mStatementsFactory.buildDeleteRelationship(userId, buddyAddress));
  }

  @Override
  protected Collection<Relationship> load(ResultSet rs)
    throws ChatDbException
  {
    Collection<Relationship> relationships = new ArrayList<Relationship>(16);
    RelationshipBuilder relationshipBuilder = new RelationshipBuilder(rs);
    while (relationshipBuilder.next())
    {
      Relationship relationship = relationshipBuilder.build();
      relationships.add(relationship);
    }
    return relationships;
  }
}