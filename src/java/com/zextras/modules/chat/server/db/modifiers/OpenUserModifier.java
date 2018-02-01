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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.zextras.modules.chat.server.InternalUser;
import com.zextras.modules.chat.server.relationship.Relationship;
import com.zextras.modules.chat.server.relationship.Relationship.RelationshipType;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.mappers.*;
import com.zextras.modules.chat.server.db.providers.UserInfo;

@Singleton
public class OpenUserModifier implements UserModifier
{
  private UserInfoMapper mUserInfoMapper;
  private RelationshipMapper mRelationshipMapper;

  @Inject
  public OpenUserModifier(
    UserInfoMapper userInfoMapper,
    RelationshipMapper relationshipMapper
  )
  {
    mUserInfoMapper = userInfoMapper;
    mRelationshipMapper = relationshipMapper;
  }

  @Override
  public int insertUser(UserInfo user)
    throws ChatDbException
  {
    return mUserInfoMapper.insert(user.getAddress());
  }

  @Override
  public int updateUser(InternalUser user)
    throws ChatDbException
  {
    return mUserInfoMapper.update(user.getEntityId(), user.getAddress());
  }

  @Override
  public int deleteUser(InternalUser user)
    throws ChatDbException
  {
    for (Relationship relationship : user.getRelationships()) {
      mRelationshipMapper.delete(user.getEntityId(), relationship.getBuddyAddress().toString());
    }

    user.markDeleted();
    return mUserInfoMapper.delete(user.getEntityId());
  }

  @Override
  public int addRelationship(int userId,
                             RelationshipType relationshipType,
                             SpecificAddress buddyAddress,
                             String buddyNickname,
                             String group)
    throws ChatDbException
  {
    return mRelationshipMapper.insert(userId, relationshipType, buddyAddress, buddyNickname, group);
  }

  @Override
  public void updateRelationship(int userId,
                                 Relationship relationship)
    throws ChatDbException
  {
    mRelationshipMapper.update(userId,
                               relationship.getType(),
                               relationship.getBuddyAddress(),
                               relationship.getBuddyNickname(),
                               relationship.getGroup());
  }
  
  @Override
  public void removeRelationship(int userId,
                                 SpecificAddress buddyAddress)
    throws
    ChatDbException
  {
    mRelationshipMapper.delete(userId,
                               buddyAddress.toString());
  
  }
}
