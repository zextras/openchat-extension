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

import com.zextras.lib.Container;
import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.address.SpecificAddress;

import com.zextras.modules.chat.server.db.PersistentEntity;
import com.zextras.modules.chat.server.db.modifiers.UserModifier;
import com.zextras.modules.chat.server.events.EventQueue;
import com.zextras.modules.chat.server.relationship.DirectRelationshipProvider;
import com.zextras.modules.chat.server.relationship.Relationship;
import com.zextras.modules.chat.server.relationship.RelationshipModifier;
import com.zextras.modules.chat.server.relationship.RelationshipProvider;

import java.util.Collection;

public class InternalUser implements User, PersistentEntity
{
  private final EventQueue                 mPersistentEventQueue;
  private final SpecificAddress            mAddress;
  private final RelationshipModifier       mRelationshipModifier;
  private final DirectRelationshipProvider mDirectRelationshipProvider;
  private final UserCapabilitiesProvider   mCapabilitiesProvider;
  private final int                        mId;
  private final UserModifier               mUserModifier;
  private final RelationshipProvider       mRelationshipProvider;
  private boolean mDeleted = false;


  public InternalUser(
    int id,
    SpecificAddress sender,
    EventQueue persistentEventQueue,
    UserModifier userModifier,
    RelationshipProvider relationshipProvider,
    RelationshipModifier relationshipModifier,
    DirectRelationshipProvider directRelationshipProvider,
    UserCapabilitiesProvider capabilitiesProvider
  )
  {
    mId = id;
    mPersistentEventQueue = persistentEventQueue;
    mAddress = sender;
    mUserModifier = userModifier;
    mRelationshipProvider = relationshipProvider;
    mRelationshipModifier = relationshipModifier;
    mDirectRelationshipProvider = directRelationshipProvider;
    mCapabilitiesProvider = capabilitiesProvider;
  }

  public EventQueue getEventQueue()
  {
    return mPersistentEventQueue;
  }

  @Override
  public int getEntityId()
  {
    return mId;
  }

  @Override
  public void updateBuddyNickname(
    SpecificAddress buddy,
    String newNickName
  )
  {
    if (isDeleted())
    {
      return;
    }
    mRelationshipModifier.updateBuddyNickname(
      mId,
      mAddress, buddy,
      newNickName
    );
  }

  @Override
  public void updateBuddyGroup(
    SpecificAddress friendToRename,
    String newGroup
  )
  {
    if (isDeleted())
    {
      return;
    }
    mRelationshipModifier.updateBuddyGroup(
      mId,
      mAddress, friendToRename,
      newGroup
    );
  }

  @Override
  public void updateRelationshipType(
    SpecificAddress buddy,
    Relationship.RelationshipType type
  )
  {
    if (isDeleted())
    {
      return;
    }
    mRelationshipModifier.updateRelationshipType(
      mId,
      mAddress, buddy,
      type
    );
  }

  @Override
  public void addRelationship(
    SpecificAddress buddy,
    Relationship.RelationshipType type,
    String buddyNickname,
    String group
  )
  {
    if (isDeleted())
    {
      return;
    }
    mRelationshipModifier.addRelationship(
      mId,
      mAddress, buddy,
      type,
      buddyNickname,
      group
    );
  }

  @Override
  public void removeRelationship(SpecificAddress target)
  {
    if (isDeleted())
    {
      return;
    }
    mRelationshipModifier.removeRelationship(
      mId,
      mAddress, target
    );
  }

  @Override
  public boolean hasAddressEquals(SpecificAddress buddyAddress)
  {
    return mAddress.equals(buddyAddress);
  }

  public void delete()
  {
    if (isDeleted())
    {
      return;
    }
    try
    {
      mUserModifier.deleteUser(this);
    }
    catch (Exception e)
    {
      ChatLog.log.warn("Cannot delete user " + mAddress + ": " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  @Override
  public Container getPublicCapabilities()
  {
    return mCapabilitiesProvider.getPublicCapabilities(this);
  }

  @Override
  public Container getCapabilities()
  {
    return mCapabilitiesProvider.getCapabilities(this);
  }

  @Override
  public boolean hasRelationship(SpecificAddress buddy)
  {
    return mRelationshipProvider.userRelationshipType(
      mId,
      mAddress, buddy
    ) != null;
  }

  @Override
  public Relationship getRelationship(SpecificAddress buddy)
  {
    return mRelationshipProvider.assertUserRelationshipByBuddyAddress(
      mId,
      mAddress, buddy
    );
  }

  @Override
  public Collection<Relationship> getRelationships()
  {
    return mRelationshipProvider.getUserRelationships(mId, mAddress);
  }

  @Override
  public Collection<Relationship> getDirectRelationships()
  {
    return mDirectRelationshipProvider.getUserRelationships(mId, mAddress);
  }

  @Override
  public boolean hasRelationshipType(
    SpecificAddress buddyAddress,
    Relationship.RelationshipType type
  )
  {
    return mRelationshipProvider.userRelationshipType(
      mId,
      mAddress, buddyAddress
    ) == type;
  }

  @Override
  public boolean hasAccepted(SpecificAddress buddyAddress)
  {
    return mRelationshipProvider.userRelationshipType(
      mId,
      mAddress, buddyAddress
    ) == Relationship.RelationshipType.ACCEPTED;
  }

  @Override
  public boolean hasBlocked(SpecificAddress buddyAddress)
  {
    return mRelationshipProvider.userRelationshipType(
      mId,
      mAddress, buddyAddress
    ) == Relationship.RelationshipType.BLOCKED;
  }

  @Override
  public boolean isPending(SpecificAddress buddyAddress)
  {
    return mRelationshipProvider.userRelationshipType(
      mId,
      mAddress, buddyAddress
    ) == Relationship.RelationshipType.NEED_RESPONSE;
  }

  public void markDeleted()
  {
    mDeleted = true;
  }

  @Override
  public boolean isDeleted()
  {
    return mDeleted;
  }

  @Override
  public SpecificAddress getAddress()
  {
    return mAddress;
  }
}
