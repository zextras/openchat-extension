/*
 * Copyright (C) 2017 ZeXtras S.r.l.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.zextras.modules.chat.server;

import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.relationship.Relationship;


import java.util.Collection;

public interface User
{
  SpecificAddress getAddress();

  boolean hasRelationship(SpecificAddress buddy);
  
  Relationship getRelationship(SpecificAddress buddy);
  
  Collection<Relationship> getRelationships();
  
  Collection<Relationship> getDirectRelationships();
  
  void updateBuddyGroup(SpecificAddress friendToRename, String newGroup);
  
  void updateBuddyNickname(SpecificAddress buddy, String newNickName);
  
  void updateRelationshipType(SpecificAddress buddy, Relationship.RelationshipType type);
  
  void addRelationship(SpecificAddress buddy, Relationship.RelationshipType type, String buddyNickname, String group);
  
  void removeRelationship(SpecificAddress target);
  
  boolean hasRelationshipType(SpecificAddress buddyAddress, Relationship.RelationshipType type);

  boolean hasAccepted(SpecificAddress buddy);

  boolean hasBlocked(SpecificAddress buddy);

  boolean isPending(SpecificAddress buddy);

  boolean isDeleted();

  boolean hasAddressEquals(SpecificAddress buddyAddress);

  void delete();
}
