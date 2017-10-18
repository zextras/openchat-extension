package com.zextras.modules.chat.server.relationship;


import com.zextras.modules.chat.server.Relationship;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.exceptions.ChatDbException;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Interface provides user's relationship's information.
 */
public interface RelationshipProvider
{
  Collection<Relationship> getUserRelationships(
    int userId,
    SpecificAddress userAddress
  );

  @Nullable
  Relationship getUserRelationshipByBuddyAddress(
    int userId,
    SpecificAddress userAddress,
    SpecificAddress buddyAddress
  );

  Relationship assertUserRelationshipByBuddyAddress(
    int userId,
    SpecificAddress userAddress,
    SpecificAddress buddyAddress
  );

  @Nullable
  Relationship.RelationshipType userRelationshipType(
    int userId,
    SpecificAddress userAddress,
    SpecificAddress buddyAddress
  );
}
