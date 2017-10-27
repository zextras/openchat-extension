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

package com.zextras.modules.chat.server.db.modifiers;

import com.zextras.modules.chat.server.InternalUser;
import com.zextras.modules.chat.server.relationship.Relationship;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.providers.UserInfo;
import com.zextras.modules.chat.server.exceptions.ChatDbException;

public interface UserModifier
{
  int insertUser(UserInfo user)
    throws ChatDbException;

  int updateUser(InternalUser user)
      throws ChatDbException;

  int deleteUser(InternalUser user)
        throws ChatDbException;

  int addRelationship(int userId,
                      Relationship.RelationshipType relationshipType,
                      SpecificAddress buddyAddress,
                      String buddyNickname,
                      String group)
          throws ChatDbException;

  void updateRelationship(int userId, Relationship relationship)
            throws ChatDbException;

  void removeRelationship(int userId, SpecificAddress buddyAddress)
              throws ChatDbException;
}
