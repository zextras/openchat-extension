package com.zextras.modules.chat.server.relationship;


import com.zextras.modules.chat.server.Relationship;
import com.zextras.modules.chat.server.address.SpecificAddress;

/**
 * Class provides user's relationship's information, independently from the
 * relationship's source.
 */
public abstract class RelationshipProviderAdapter implements RelationshipProvider
{
  @Override
  public Relationship assertUserRelationshipByBuddyAddress(
    int userId,
    SpecificAddress userAddress, SpecificAddress buddyAddress
  )
  {
    Relationship relationship = getUserRelationshipByBuddyAddress(
      userId,
      userAddress,
      buddyAddress
    );
    if (relationship == null)
    {
      throw new RuntimeException();
    }
    return relationship;
  }
}
