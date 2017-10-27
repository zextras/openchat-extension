package com.zextras.modules.chat.server.relationship;

import com.google.inject.Inject;
import com.zextras.modules.chat.server.address.SpecificAddress;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

/**
 * Class provides user's direct-relationship's information.
 * A direct-relationship is a relationship create from a user with another
 * user.
 */
public class DirectRelationshipProvider extends RelationshipProviderAdapter
{
  private final DirectRelationshipStorage mDirectRelationshipStorage;

  @Inject
  public DirectRelationshipProvider(DirectRelationshipStorage directRelationshipStorage)
  {
    mDirectRelationshipStorage = directRelationshipStorage;
  }

  @Override
  public Collection<Relationship> getUserRelationships(int userId, SpecificAddress userAddress)
  {
    return mDirectRelationshipStorage.get(userId);
  }

  /**
   * Method created for performance purposes
   */
  public Map<SpecificAddress, Relationship> getUserRelationshipsMap(int userId)
  {
    return mDirectRelationshipStorage.getMap(userId);
  }

  @Override
  public Relationship getUserRelationshipByBuddyAddress(
    int userId,
    SpecificAddress userAddress, SpecificAddress buddyAddress
  )
  {
    return mDirectRelationshipStorage.get(userId, buddyAddress.withoutResource());
  }

  @Nullable
  @Override
  public Relationship.RelationshipType userRelationshipType(int userId, SpecificAddress userAddress, SpecificAddress buddyAddress)
  {
    Relationship relationship = mDirectRelationshipStorage.get(userId, buddyAddress.withoutResource());
    return relationship == null ? null : relationship.getType();
  }
}
