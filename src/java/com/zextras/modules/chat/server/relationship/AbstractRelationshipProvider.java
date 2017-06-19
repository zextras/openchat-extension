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


import com.zextras.modules.chat.server.Relationship;
import com.zextras.modules.chat.server.address.SpecificAddress;

/**
 * Class provides user's relationship's information, independently from the
 * relationship's source.
 */
public abstract class AbstractRelationshipProvider
  implements RelationshipProvider
{
  @Override
  public Relationship assertUserRelationshipByBuddyAddress(int userId,
                                                           SpecificAddress buddyAddress)
  {
    Relationship relationship = getUserRelationshipByBuddyAddress(userId,
                                                                  buddyAddress);
    if (relationship == null)
    {
      throw new RuntimeException();
    }
    return relationship;
  }
  
  @Override
  public boolean userHasRelationship(int userId,
                                     SpecificAddress buddyAddress)
  {
    return getUserRelationshipByBuddyAddress(userId,
                                             buddyAddress) != null;
  }
  
  @Override
  public boolean userHasRelationshipWithType(int userId,
                                             SpecificAddress buddyAddress,
                                             Relationship.RelationshipType type)
  {
    Relationship relationship = getUserRelationshipByBuddyAddress(userId,
                                                                  buddyAddress);
    
    if (relationship != null)
    {
      return relationship.getType() == type;
    }
    
    return false;
  }
  
  @Override
  public boolean userHasAcceptedRelationship(int userId,
                                             SpecificAddress buddyAddress)
  {
    return userHasRelationshipWithType(userId,
                                       buddyAddress,
                                       Relationship.RelationshipType.ACCEPTED);
  }
  
  @Override
  public boolean userHasBlockedRelationship(int userId,
                                            SpecificAddress buddyAddress)
  {
    return userHasRelationshipWithType(userId,
                                       buddyAddress,
                                       Relationship.RelationshipType.BLOCKED);
  }
  
  @Override
  public boolean userIsPendingRelationship(int userId,
                                           SpecificAddress buddyAddress)
  {
    return userHasRelationshipWithType(userId,
                                       buddyAddress,
                                       Relationship.RelationshipType.NEED_RESPONSE);
  }
}
