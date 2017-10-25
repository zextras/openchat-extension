package com.zextras.modules.chat.server.relationship;

import com.google.inject.Inject;
import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.modifiers.UserModifier;
import org.openzal.zal.Utils;

/**
 * Class provides user's direct-relationship's modifier methods.
 *
 * @see DirectRelationshipProvider
 */
public class DirectRelationshipModifier
  implements RelationshipModifier
{
  private final UserModifier mUserModifier;
  private final DirectRelationshipStorage mDirectRelationshipStorage;

  @Inject
  public DirectRelationshipModifier(
    UserModifier userModifier,
    DirectRelationshipStorage directRelationshipStorage
  )
  {
    mUserModifier = userModifier;
    mDirectRelationshipStorage = directRelationshipStorage;
  }

  @Override
  public void updateBuddyNickname(
    int userId,
    SpecificAddress userAddress, SpecificAddress buddyAddress,
    String newNickName
  )
  {
    Relationship targetRelationship = mDirectRelationshipStorage.assertRelationship(userId, buddyAddress);

    targetRelationship.updateVolatileNickname(newNickName);
    updateStoredRelationship(
      targetRelationship,
      userId
    );
  }

  @Override
  public void updateBuddyGroup(
    int userId,
    SpecificAddress userAddress, SpecificAddress buddyAddress,
    String newGroupName
  )
  {
    Relationship targetRelationship = mDirectRelationshipStorage.assertRelationship(userId, buddyAddress);

    targetRelationship.updateVolatileGroup(newGroupName);
    updateStoredRelationship(
      targetRelationship,
      userId
    );
  }

  @Override
  public void updateRelationshipType(
    int userId,
    SpecificAddress userAddress, SpecificAddress buddyAddress,
    Relationship.RelationshipType newType
  )
  {
    Relationship targetRelationship = mDirectRelationshipStorage.assertRelationship(userId, buddyAddress);

    targetRelationship.updateVolatileType(newType);
    updateStoredRelationship(
      targetRelationship,
      userId
    );
  }

  @Override
  public void addRelationship(
    int userId,
    SpecificAddress userAddress,
    SpecificAddress buddyAddress,
    Relationship.RelationshipType type,
    String buddyNickname,
    String group
  )
  {
    Relationship targetRelationship = mDirectRelationshipStorage.get(userId, buddyAddress);
    if( targetRelationship != null ) {
      return;
    }

    Relationship newRelationship = new Relationship(
      buddyAddress,
      type,
      buddyNickname,
      group
    );

    mDirectRelationshipStorage.upsertRelationship(userId, newRelationship);
    try
    {
      mUserModifier.addRelationship(
        userId,
        newRelationship.getType(),
        newRelationship.getBuddyAddress(),
        newRelationship.getBuddyNickname(),
        newRelationship.getGroup()
      );
    }
    catch (Exception e)
    {
      ChatLog.log.warn("Cannot add relationship from " + newRelationship
        .getBuddyAddress() + " to user with id " + userId +
                         ": " +
                         e.getMessage());
      ChatLog.log.debug(Utils.exceptionToString(e));
      throw new RuntimeException(e);
    }
  }

  @Override
  public void removeRelationship(
    int userId,
    SpecificAddress userAddress, SpecificAddress buddyAddress
  )
  {
    mDirectRelationshipStorage.removeRelationship(userId,buddyAddress);

    try
    {
      mUserModifier.removeRelationship(
        userId,
        buddyAddress
      );
    }
    catch (Exception e)
    {
      ChatLog.log.warn("Cannot remove relationship " + buddyAddress + " from  " +
                         userId + ": " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  private void updateStoredRelationship(
    Relationship updatedRelationship,
    int userId
  )
  {
    try
    {
      mUserModifier.updateRelationship(
        userId,
        updatedRelationship
      );
    }
    catch (Exception e)
    {
      ChatLog.log.warn("Cannot update relationship of user with id " +
                         userId + " " +
                         "with " + updatedRelationship.getBuddyAddress() + ": " + e.getMessage());
      throw new RuntimeException(e);
    }
  }
}
