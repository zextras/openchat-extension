package com.zextras.modules.chat.server.relationship;


import com.zextras.modules.chat.server.Relationship;
import com.zextras.modules.chat.server.address.SpecificAddress;

/**
 * Interface provides user's relationships's modifier methods.
 */
public interface RelationshipModifier
{
  void addRelationship(int userId,
                       SpecificAddress buddyAddress,
                       Relationship.RelationshipType type,
                       String buddyNickname,
                       String group);
  
  void removeRelationship(int userId,
                          SpecificAddress buddyAddress);
  
  void updateBuddyNickname(int userId,
                           SpecificAddress buddyAddress,
                           String newNickName);
  
  void updateBuddyGroup(int userId,
                        SpecificAddress buddyAddress,
                        String newGroupName);
  
  void updateRelationshipType(int userId,
                              SpecificAddress buddyAddress,
                              Relationship.RelationshipType newType);
}
