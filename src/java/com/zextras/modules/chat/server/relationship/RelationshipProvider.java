package com.zextras.modules.chat.server.relationship;


import com.zextras.modules.chat.server.Relationship;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.exceptions.ChatDbException;

import java.util.Collection;

/**
 * Interface provides user's relationship's information.
 */
public interface RelationshipProvider
{
  Collection<Relationship> getUserRelationships(int userId);
  
  Relationship getUserRelationshipByBuddyAddress(int userId,
                                                 SpecificAddress buddyAddress);
  
  Relationship assertUserRelationshipByBuddyAddress(int userId,
                                                    SpecificAddress buddyAddress);
  
  
  boolean userHasRelationship(int userId,
                              SpecificAddress buddyAddress);
  
  boolean userHasRelationshipWithType(int userId,
                                      SpecificAddress buddyAddress,
                                      Relationship.RelationshipType type);
  
  boolean userHasAcceptedRelationship(int userId,
                                      SpecificAddress buddyAddress);
  
  boolean userHasBlockedRelationship(int userId,
                                     SpecificAddress buddyAddress);
  
  boolean userIsPendingRelationship(int userId,
                                    SpecificAddress buddyAddress);
}
