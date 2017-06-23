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
