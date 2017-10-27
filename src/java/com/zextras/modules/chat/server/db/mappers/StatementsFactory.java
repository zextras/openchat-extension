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

import com.zextras.modules.chat.server.relationship.Relationship;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.sql.SqlStatement;

public interface StatementsFactory
{
  SqlStatement buildSelectUser(SpecificAddress address);
  SqlStatement buildSelectUser(int address);
  SqlStatement buildInsertUser(SpecificAddress address);
  SqlStatement buildUpdateUser(int userId, SpecificAddress address);
  SqlStatement buildDeleteUser(int userId);
  SqlStatement buildSelectAllUsers();
  SqlStatement buildSelectRelationships(int userId);
  SqlStatement buildInsertRelationship(int userId, Relationship.RelationshipType relationshipType, SpecificAddress buddyAddress, String buddyNickname, String group);
  SqlStatement buildUpdateRelationship(int relationshipId, Relationship.RelationshipType relationshipType, SpecificAddress buddyAddress, String buddyNickname, String group);
  SqlStatement buildDeleteRelationship(int userId, String buddyAddress);
  SqlStatement buildDbInfo();
}
