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
import com.zextras.modules.chat.server.Relationship;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.exceptions.ChatDbException;

/**
 * Class provides user's relationship's modifier methods, hiding the
 * relationships's fonts.
 */
public class RelationshipModifierProxy
  implements RelationshipModifier
{
  private final DirectRelationshipModifier            mDirectRelationshipModifier;
  private final DistributionListRelationshipModifier  mDistributionListRelationshipModifier;
  private final DistributionListsRelationshipProvider mDistributionListsRelationshipProvider;
  
  @Inject
  public RelationshipModifierProxy(DirectRelationshipModifier directRelationshipModifier,
                                   DistributionListRelationshipModifier distributionListRelationshipModifier,
                                   DistributionListsRelationshipProvider distributionListsRelationshipProvider)
    throws
    ChatDbException
  {
    mDirectRelationshipModifier = directRelationshipModifier;
    mDistributionListRelationshipModifier = distributionListRelationshipModifier;
    mDistributionListsRelationshipProvider = distributionListsRelationshipProvider;
  }
  
  @Override
  public void addRelationship(int userId,
                              SpecificAddress buddyAddress,
                              Relationship.RelationshipType type,
                              String buddyNickname,
                              String group)
  {
    mDirectRelationshipModifier.addRelationship(userId,
                                                buddyAddress,
                                                type,
                                                buddyNickname,
                                                group);
  }
  
  @Override
  public void removeRelationship(int userId,
                                 SpecificAddress buddyAddress)
  {
    if (mDistributionListsRelationshipProvider.userHasRelationship(userId,
                                                                   buddyAddress))
    {
      mDistributionListRelationshipModifier.removeRelationship(userId,
                                                               buddyAddress);
    }
    else
    {
      mDirectRelationshipModifier.removeRelationship(userId,
                                                     buddyAddress);
    }
  }
  
  @Override
  public void updateBuddyNickname(int userId,
                                  SpecificAddress buddyAddress,
                                  String newNickName)
  {
    if (mDistributionListsRelationshipProvider.userHasRelationship(userId,
                                                                   buddyAddress))
    {
      mDistributionListRelationshipModifier.updateBuddyNickname(userId,
                                                                buddyAddress,
                                                                newNickName);
    }
    else
    {
      mDirectRelationshipModifier.updateBuddyNickname(userId,
                                                      buddyAddress,
                                                      newNickName);
    }
  }
  
  @Override
  public void updateBuddyGroup(int userId,
                               SpecificAddress buddyAddress,
                               String newGroupName)
  {
    if (mDistributionListsRelationshipProvider.userHasRelationship(userId,
                                                                   buddyAddress))
    {
      mDistributionListRelationshipModifier.updateBuddyGroup(userId,
                                                             buddyAddress,
                                                             newGroupName);
    }
    else
    {
      mDirectRelationshipModifier.updateBuddyGroup(userId,
                                                   buddyAddress,
                                                   newGroupName);
    }
  }
  
  @Override
  public void updateRelationshipType(int userId,
                                     SpecificAddress buddyAddress,
                                     Relationship.RelationshipType newType)
  {
    if (mDistributionListsRelationshipProvider.userHasRelationship(userId,
                                                                   buddyAddress))
    {
      mDistributionListRelationshipModifier.updateRelationshipType(userId,
                                                                   buddyAddress,
                                                                   newType);
    }
    else
    {
      mDirectRelationshipModifier.updateRelationshipType(userId,
                                                         buddyAddress,
                                                         newType);
    }
  }
}
