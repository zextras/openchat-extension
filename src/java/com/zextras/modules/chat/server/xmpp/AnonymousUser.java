/*
 * ZAL - The abstraction layer for Zimbra.
 * Copyright (C) 2017 ZeXtras S.r.l.
 *
 * This file is part of ZAL.
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
 * You should have received a copy of the GNU General Public License
 * along with ZAL. If not, see <http://www.gnu.org/licenses/>.
 */

package com.zextras.modules.chat.server.xmpp;

import com.zextras.modules.chat.server.*;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.relationship.Relationship;

import java.util.Collection;
import java.util.Collections;

public class AnonymousUser implements User
{
  @Override
  public SpecificAddress getAddress()
  {
    throw new IllegalStateException();
  }

  @Override
  public boolean hasRelationship(SpecificAddress buddy)
  {
    return false;
  }

  @Override
  public Relationship getRelationship(SpecificAddress buddy)
  {
    throw new IllegalStateException();
  }

  @Override
  public Collection<Relationship> getRelationships()
  {
    return Collections.emptyList();
  }
  
  @Override
  public Collection<Relationship> getDirectRelationships()
  {
    return Collections.emptyList();
  }
  
  @Override
  public void updateBuddyGroup(SpecificAddress friendToRename, String newGroup)
  {
    throw new IllegalStateException();
  }

  @Override
  public void updateBuddyNickname(SpecificAddress buddy, String newNickName)
  {
    throw new IllegalStateException();
  }

  @Override
  public void updateRelationshipType(SpecificAddress buddy, Relationship.RelationshipType type)
  {
    throw new IllegalStateException();
  }

  @Override
  public void addRelationship(SpecificAddress buddy, Relationship.RelationshipType type, String buddyNickname, String group)
  {
    throw new IllegalStateException();
  }

  @Override
  public void removeRelationship(SpecificAddress target)
  {
    throw new IllegalStateException();
  }

  @Override
  public boolean hasRelationshipType(SpecificAddress buddyAddress, Relationship.RelationshipType type)
  {
    throw new IllegalStateException();
  }

  @Override
  public boolean hasAccepted(SpecificAddress buddy)
  {
    return false;
  }

  @Override
  public boolean hasBlocked(SpecificAddress buddy)
  {
    return false;
  }

  @Override
  public boolean isPending(SpecificAddress buddy)
  {
    return false;
  }

  @Override
  public boolean isDeleted() {
    return false;
  }

  @Override
  public void delete()
  {
  }

  @Override
  public boolean hasAddressEquals(SpecificAddress buddyAddress)
  {
    return false;
  }
}
