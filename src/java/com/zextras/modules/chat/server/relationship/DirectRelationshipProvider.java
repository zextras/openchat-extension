package com.zextras.modules.chat.server.relationship;

import com.google.inject.Inject;
import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.Relationship;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.mappers.RelationshipMapper;
import com.zextras.modules.chat.server.exceptions.ChatDbException;

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
  public Map<SpecificAddress, Relationship> getUserRelationshipsMap(int userId, SpecificAddress userAddress)
  {
    return mDirectRelationshipStorage.getMap(userId,userAddress);
  }

  @Override
  public Relationship getUserRelationshipByBuddyAddress(
    int userId,
    SpecificAddress userAddress, SpecificAddress buddyAddress
  )
  {
    return mDirectRelationshipStorage.get(userId, buddyAddress);
  }

  @Nullable
  @Override
  public Relationship.RelationshipType userRelationshipType(int userId, SpecificAddress userAddress, SpecificAddress buddyAddress)
  {
    Relationship relationship = mDirectRelationshipStorage.get(userId, buddyAddress);
    return relationship == null ? null : relationship.getType();
  }
}
