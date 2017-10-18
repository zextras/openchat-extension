package com.zextras.modules.chat.server.relationship;

import com.google.inject.Inject;
import com.zextras.modules.chat.server.Relationship;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.exceptions.ChatDbException;

/**
 * Class provides user's relationship's modifier methods, hiding the
 * relationships's fonts.
 */
public class RelationshipModifierProxy implements RelationshipModifier
{
  private final DirectRelationshipModifier            mDirectRelationshipModifier;
  private final DistributionListRelationshipModifier  mDistributionListRelationshipModifier;
  private final DistributionListsRelationshipProvider mDistributionListsRelationshipProvider;

  @Inject
  public RelationshipModifierProxy(DirectRelationshipModifier directRelationshipModifier,
                                   DistributionListRelationshipModifier distributionListRelationshipModifier,
                                   DistributionListsRelationshipProvider distributionListsRelationshipProvider)
  {
    mDirectRelationshipModifier = directRelationshipModifier;
    mDistributionListRelationshipModifier = distributionListRelationshipModifier;
    mDistributionListsRelationshipProvider = distributionListsRelationshipProvider;
  }

  private boolean dlistHasFriendship(int userId, SpecificAddress userAddress, SpecificAddress buddyAddress)
  {
    return mDistributionListsRelationshipProvider.userRelationshipType(
      userId,
      userAddress,
      buddyAddress
    ) != null;
  }

  @Override
  public void addRelationship(
    int userId,
    SpecificAddress userAddress, SpecificAddress buddyAddress,
    Relationship.RelationshipType type,
    String buddyNickname,
    String group
  )
  {
    mDirectRelationshipModifier.addRelationship(userId,
                                                userAddress, buddyAddress,
                                                type,
                                                buddyNickname,
                                                group);
  }

  @Override
  public void removeRelationship(
    int userId,
    SpecificAddress userAddress, SpecificAddress buddyAddress
  )
  {
    if (dlistHasFriendship(userId, userAddress, buddyAddress))
    {
      mDistributionListRelationshipModifier.removeRelationship(userId,
                                                               userAddress, buddyAddress
      );
    }
    else
    {
      mDirectRelationshipModifier.removeRelationship(userId,
                                                     userAddress, buddyAddress
      );
    }
  }

  @Override
  public void updateBuddyNickname(
    int userId,
    SpecificAddress userAddress, SpecificAddress buddyAddress,
    String newNickName
  )
  {
    if (dlistHasFriendship(userId, userAddress, buddyAddress))
    {
      mDistributionListRelationshipModifier.updateBuddyNickname(userId,
                                                                userAddress, buddyAddress,
                                                                newNickName);
    }
    else
    {
      mDirectRelationshipModifier.updateBuddyNickname(userId,
                                                      userAddress, buddyAddress,
                                                      newNickName);
    }
  }

  @Override
  public void updateBuddyGroup(
    int userId,
    SpecificAddress userAddress, SpecificAddress buddyAddress,
    String newGroupName
  )
  {
    if (dlistHasFriendship(userId, userAddress, buddyAddress))
    {
      mDistributionListRelationshipModifier.updateBuddyGroup(userId,
                                                             userAddress, buddyAddress,
                                                             newGroupName);
    }
    else
    {
      mDirectRelationshipModifier.updateBuddyGroup(userId,
                                                   userAddress, buddyAddress,
                                                   newGroupName);
    }
  }

  @Override
  public void updateRelationshipType(
    int userId,
    SpecificAddress userAddress, SpecificAddress buddyAddress,
    Relationship.RelationshipType newType
  )
  {
    if (dlistHasFriendship(userId, userAddress, buddyAddress))
    {
      mDistributionListRelationshipModifier.updateRelationshipType(userId,
                                                                   userAddress, buddyAddress,
                                                                   newType);
    }
    else
    {
      mDirectRelationshipModifier.updateRelationshipType(userId,
                                                         userAddress, buddyAddress,
                                                         newType);
    }
  }
}
