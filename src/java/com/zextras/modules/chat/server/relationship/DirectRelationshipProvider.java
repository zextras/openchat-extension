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

package com.zextras.modules.chat.server.relationship;

import com.google.inject.Inject;
import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.Relationship;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.mappers.RelationshipMapper;
import com.zextras.modules.chat.server.exceptions.ChatDbException;

import java.util.Collection;
/**
 * Class provides user's direct-relationship's information.
 * A direct-relationship is a relationship create from a user with another
 * user.
 */
public class DirectRelationshipProvider
  extends AbstractRelationshipProvider
  implements RelationshipProvider
{
  final RelationshipMapper mRelationshipMapper;
  
  @Inject
  public DirectRelationshipProvider(RelationshipMapper relationshipMapper)
  {
    mRelationshipMapper = relationshipMapper;
  }
  
  @Override
  public Collection<Relationship> getUserRelationships(int userId)
  {
    try
    {
      return mRelationshipMapper.get(userId);
    }
    catch (ChatDbException e)
    {
      String message = "Error while trying to access to relationships " +
                             "of user with id " + userId;
      ChatLog.log.warn(message);
      throw new RuntimeException(message);
    }
  }
  
  @Override
  public Relationship getUserRelationshipByBuddyAddress(int userId,
                                                        SpecificAddress buddyAddress)
  {
    Collection<Relationship> mRelationships = getUserRelationships(userId);
    for (Relationship relationship : mRelationships)
    {
      if (relationship.getBuddyAddress()
                      .equals(buddyAddress.withoutResource()))
      {
        return relationship;
      }
    }
    return null;
  }
  
  
}
