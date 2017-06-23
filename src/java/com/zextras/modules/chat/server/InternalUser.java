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

import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.address.SpecificAddress;

import com.zextras.modules.chat.server.db.PersistentEntity;
import com.zextras.modules.chat.server.db.modifiers.OpenUserModifier;
import com.zextras.modules.chat.server.db.modifiers.UserModifier;
import com.zextras.modules.chat.server.events.EventQueue;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.StatusDoesNotExistException;
import com.zextras.modules.chat.server.relationship.DirectRelationshipProvider;
import com.zextras.modules.chat.server.relationship.RelationshipModifier;
import com.zextras.modules.chat.server.relationship.RelationshipProvider;
import com.zextras.modules.chat.server.status.Status;
import com.zextras.modules.chat.server.status.StatusId;
import org.openzal.zal.Utils;

import java.util.Collection;

public class InternalUser implements User, PersistentEntity
{
  private final EventQueue           mPersistentEventQueue;
  private final SpecificAddress      mAddress;
  private final RelationshipModifier mRelationshipModifier;
  private final DirectRelationshipProvider mDirectRelationshipProvider;
  private final int                  mId;
  private final UserModifier         mUserModifier;
  private final RelationshipProvider mRelationshipProvider;
  private       boolean              mDeleted = false;
  
  
  public InternalUser(int id,
                      final SpecificAddress sender,
                      final EventQueue persistentEventQueue,
                      final UserModifier userModifier,
                      final RelationshipProvider relationshipProvider,
                      final RelationshipModifier relationshipModifier,
                      final
                      DirectRelationshipProvider directRelationshipProvider)
  {
    mId = id;
    mPersistentEventQueue = persistentEventQueue;
    mAddress = sender;
    mUserModifier = userModifier;
    mRelationshipProvider = relationshipProvider;
    mRelationshipModifier = relationshipModifier;
    mDirectRelationshipProvider = directRelationshipProvider;
  }

  public EventQueue getEventQueue()
  {
    return mPersistentEventQueue;
  }

  @Override
  public int getEntityId() {
    return mId;
  }

  @Override
  public void updateBuddyNickname(final SpecificAddress buddy,
                                  final String newNickName)
  {
    if (isDeleted())
    {
      return;
    }
    mRelationshipModifier.updateBuddyNickname(mId,
                                              buddy,
                                              newNickName);
  }

  @Override
  public void updateBuddyGroup(final SpecificAddress buddy,
                               final String newGroup)
  {
    if (isDeleted())
    {
      return;
    }
    mRelationshipModifier.updateBuddyGroup(mId,
                                           buddy,
                                           newGroup);
  }

  @Override
  public void updateRelationshipType(final SpecificAddress buddy,
                                     final Relationship.RelationshipType type)
  {
    if (isDeleted())
    {
      return;
    }
    mRelationshipModifier.updateRelationshipType(mId,
                                                 buddy,
                                                 type);
  }
  
  @Override
  public void addRelationship(final SpecificAddress buddy,
                              final Relationship.RelationshipType type,
                              final String buddyNickname,
                              final String group)
  {
    if (isDeleted())
    {
      return;
    }
    mRelationshipModifier.addRelationship(mId,
                                          buddy,
                                          type,
                                          buddyNickname,
                                          group);
  }

  @Override
  public void removeRelationship(final SpecificAddress target)
  {
    if (isDeleted())
    {
      return;
    }
    mRelationshipModifier.removeRelationship(mId,
                                             target);
  }

  @Override
  public boolean hasAddressEquals(SpecificAddress buddyAddress)
  {
    return mAddress.equals(buddyAddress);
  }

  public void delete() {
    if (isDeleted()) { return; }
    try {
      mUserModifier.deleteUser(this);
    } catch (Exception e) {
      ChatLog.log.warn("Cannot delete user " + mAddress + ": " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean hasRelationship(SpecificAddress buddy)
  {
    return mRelationshipProvider.userHasRelationship(mId,
                                                     buddy);
  }

  @Override
  public Relationship getRelationship(SpecificAddress buddy)
  {
    return mRelationshipProvider.assertUserRelationshipByBuddyAddress(mId,
                                                                      buddy);
  }

  @Override
  public Collection<Relationship> getRelationships()
  {
    return mRelationshipProvider.getUserRelationships(mId);
  }
  
  @Override
  public Collection<Relationship> getDirectRelationships()
  {
    return mDirectRelationshipProvider.getUserRelationships(mId);
  }

  @Override
  public boolean hasRelationshipType(SpecificAddress buddyAddress,
                                     Relationship.RelationshipType type)
  {
    return mRelationshipProvider.userHasRelationshipWithType(mId,
                                                             buddyAddress,
                                                             type);
  }

  @Override
  public boolean hasAccepted(SpecificAddress buddy)
  {
    return mRelationshipProvider.userHasAcceptedRelationship(mId,
                                                             buddy);
  }

  @Override
  public boolean hasBlocked(SpecificAddress buddy)
  {
    return mRelationshipProvider.userHasBlockedRelationship(mId,
                                                            buddy);
  }

  @Override
  public boolean isPending(SpecificAddress buddy)
  {
    return mRelationshipProvider.userIsPendingRelationship(mId,
                                                           buddy);
  }

  public void markDeleted() {
    mDeleted = true;
  }

  @Override
  public boolean isDeleted() {
    return mDeleted;
  }

  @Override
  public SpecificAddress getAddress()
  {
    return mAddress;
  }
}
