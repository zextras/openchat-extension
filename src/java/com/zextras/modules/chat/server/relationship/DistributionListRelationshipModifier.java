package com.zextras.modules.chat.server.relationship;

import com.google.inject.Inject;
import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.address.SpecificAddress;

/**
 * Class provides user's distribution-list-relationship's modifier methods.
 *
 * @see DistributionListsRelationshipProvider
 */
public class DistributionListRelationshipModifier
  implements RelationshipModifier
{
  private final static String sDefaultGroupName = "";
  private final DirectRelationshipProvider            mDirectRelationshipProvider;
  private final DirectRelationshipModifier            mDirectRelationshipModifier;

  @Inject
  public DistributionListRelationshipModifier(
    DirectRelationshipProvider directRelationshipProvider,
    DirectRelationshipModifier directRelationshipModifier
  )
  {
    mDirectRelationshipProvider = directRelationshipProvider;
    mDirectRelationshipModifier = directRelationshipModifier;
  }

  private boolean directHasFriendship(int userId, SpecificAddress userAddress, SpecificAddress buddyAddress)
  {
    return mDirectRelationshipProvider.userRelationshipType(
      userId,
      userAddress,
      buddyAddress
    ) != null;
  }

  @Override
  public void updateBuddyNickname(
    int userId,
    SpecificAddress userAddress, SpecificAddress buddyAddress,
    String newNickName
  )
  {
    if (directHasFriendship(userId, userAddress, buddyAddress))
    {
      mDirectRelationshipModifier.updateBuddyNickname(userId,
                                                      userAddress, buddyAddress,
                                                      newNickName
      );
    }
    else
    {
      /*
      if buddy is only in the buddyGroupRelationship, it memorize the
       nickname in a direct relationship
       */
      mDirectRelationshipModifier.addRelationship(userId,
                                                  userAddress, buddyAddress,
                                                  Relationship.RelationshipType.NEED_RESPONSE,
                                                  newNickName,
                                                  sDefaultGroupName
      );
    }
  }

  @Override
  public void updateBuddyGroup(
    int userId,
    SpecificAddress userAddress, SpecificAddress buddyAddress,
    String newGroupName
  )
  {
    if (directHasFriendship(userId, userAddress, buddyAddress))
    {
      mDirectRelationshipModifier.updateBuddyGroup(userId,
                                                   userAddress, buddyAddress,
                                                   newGroupName
      );
    }
    else
    {
      /*
      if buddy is only in the buddyGroupRelationship, it memorize the
       group name in a direct relationship
       */
      mDirectRelationshipModifier.addRelationship(userId,
                                                  userAddress, buddyAddress,
                                                  Relationship.RelationshipType.NEED_RESPONSE,
                                                  buddyAddress.toString(),
                                                  newGroupName
      );
    }
  }

  @Override
  public void updateRelationshipType(
    int userId,
    SpecificAddress userAddress, SpecificAddress buddyAddress,
    Relationship.RelationshipType newType
  )
  {
    final String message =
      "Trying to update a buddy(" + buddyAddress + ") present in a " +
        "buddyDistributionList of the user with id " + userId;
    ChatLog.log.warn(message);
    throw new UnsupportedOperationException(message);
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
    final String message = "Trying to add a buddy(" + buddyAddress + ") to a " +
      "distributionListRelationship of the user with id " +
      userId;
    ChatLog.log.warn(message);
    throw new UnsupportedOperationException(message);
  }

  @Override
  public void removeRelationship(
    int userId,
    SpecificAddress userAddress, SpecificAddress buddyAddress
  )
  {
    final String message =
      "Trying to delete a buddy(" + buddyAddress.toString() + ") " +
        "present in a " + "buddyDistributionList of the user with id " +
        userId;
    ChatLog.log.warn(message);
    throw new UnsupportedOperationException(message);
  }
}
