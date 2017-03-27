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

package com.zextras.modules.chat.server;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.PersistentEntity;
import com.zextras.modules.chat.server.status.Status;
import com.zextras.utils.Jsonable;
import com.zextras.utils.ToJSONSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a UNIDIRECTIONAL buddy-relationship
 * between two users.
 * A proper, full friendship exists only when both users have
 * two corresponding relationships of type 'ACCEPTED'.
 *
 */
public class Relationship
{
  public       int             mUserId;
  public final SpecificAddress mBuddyAddress;

  /**
   * The name displayed in mUser's buddy list for mBuddy
   */
  private String           mBuddyNickname;
  private RelationshipType mType;
  private String           mGroup;

  public int getUserId()
  {
    return mUserId;
  }

  @JsonSerialize(using = ToJSONSerializer.class)
  public static enum RelationshipType implements Jsonable
  {
    UNKNOWN(-1), ACCEPTED(0), BLOCKED(1), NEED_RESPONSE(2), INVITED(3);

    private static final Map<Byte, RelationshipType> mByteToType = new HashMap<Byte, RelationshipType>();

    static
    {
      for (RelationshipType type : RelationshipType.values())
      {
        mByteToType.put(type.toByte(), type);
      }
    }

    private final byte mTypeByte;

    RelationshipType(int typeByte)
    {
      mTypeByte = (byte) typeByte;
    }

    public byte toByte()
    {
      return mTypeByte;
    }

    public Status.StatusType toStatusType()
    {
      if( ordinal() == NEED_RESPONSE.ordinal() ) {
        return Status.StatusType.NEED_RESPONSE;
      }

      if( ordinal() == INVITED.ordinal() ) {
        return Status.StatusType.INVITED;
      }

      if( ordinal() == BLOCKED.ordinal() ) {
        return Status.StatusType.UNREACHABLE;
      }

      throw new RuntimeException("Unable to convert status type "+toString());
    }

    public Byte toJSON()
    {
      return toByte();
    }

    public static RelationshipType fromByte(byte value)
    {
      RelationshipType type = mByteToType.get(value);
      return type != null ? type : UNKNOWN;
    }
  }

  public Relationship(
    int userId, SpecificAddress buddyAddress,
    RelationshipType type,
    String buddyNickname,
    String group
  )
  {
    mUserId = userId;
    mType = type;
    mBuddyAddress = buddyAddress;
    mBuddyNickname = buddyNickname;
    mGroup = group;
  }

  public SpecificAddress getBuddyAddress() {
    return mBuddyAddress;
  }

  public String getBuddyNickname() {
    return mBuddyNickname;
  }

  public RelationshipType getType() {
    return mType;
  }

  public String getGroup()
  {
    return mGroup;
  }

  public boolean isType(RelationshipType relationshipType) {
    return getType().equals(relationshipType);
  }

  public void updateVolatileNickname(String newNickName) {
    mBuddyNickname = newNickName;
  }

  public void updateVolatileType(RelationshipType type) {
    mType = type;
  }
  public void updateVolatileGroup(String group) {
    mGroup = group;
  }
  
  @Override
  public boolean equals(final Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }
    
    final Relationship that = (Relationship) o;
    
    if (mUserId != that.mUserId)
    {
      return false;
    }
    if (mBuddyAddress != null
        ? !mBuddyAddress.equals(that.mBuddyAddress)
        : that.mBuddyAddress != null)
    {
      return false;
    }
    if (mBuddyNickname != null
        ? !mBuddyNickname.equals(that.mBuddyNickname)
        : that.mBuddyNickname != null)
    {
      return false;
    }
    if (mType != that.mType)
    {
      return false;
    }
    if (mGroup != null
        ? !mGroup.equals(that.mGroup)
        : that.mGroup != null)
    {
      return false;
    }
    
    return true;
  }
  
  @Override
  public int hashCode()
  {
    int result = mUserId;
    result = 31 * result + (mBuddyAddress != null
                            ? mBuddyAddress.hashCode()
                            : 0);
    result = 31 * result + (mBuddyNickname != null
                            ? mBuddyNickname.hashCode()
                            : 0);
    result = 31 * result + (mType != null
                            ? mType.hashCode()
                            : 0);
    result = 31 * result + (mGroup != null
                            ? mGroup.hashCode()
                            : 0);
    return result;
  }
}
