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
