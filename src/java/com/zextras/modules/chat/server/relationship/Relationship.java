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

package com.zextras.modules.chat.server.relationship;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.zextras.modules.chat.server.address.SpecificAddress;
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
  public final SpecificAddress mBuddyAddress;

  /**
   * The name displayed in mUser's buddy list for mBuddy
   */
  private String           mBuddyNickname;
  private RelationshipType mType;
  private String           mGroup;

  public Relationship copy()
  {
    return new Relationship(
      mBuddyAddress,
      mType,
      mBuddyNickname,
      mGroup
    );
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

  @Override
  public String toString()
  {
    return "Relationship{" +
      "mBuddyAddress=" + mBuddyAddress +
      ", mBuddyNickname='" + mBuddyNickname + '\'' +
      ", mType=" + mType +
      ", mGroup='" + mGroup + '\'' +
      '}';
  }

  public Relationship(
    SpecificAddress buddyAddress,
    RelationshipType type,
    String buddyNickname,
    String group
  )
  {
    mType = type;
    mBuddyAddress = buddyAddress.intern();
    mBuddyNickname = buddyNickname.intern();
    mGroup = group.intern();
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

  void updateVolatileNickname(String newNickName) {
    mBuddyNickname = newNickName;
  }
  void updateVolatileType(RelationshipType type) {
    mType = type;
  }
  void updateVolatileGroup(String group) {
    mGroup = group;
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Relationship that = (Relationship) o;

    if (!mBuddyAddress.equals(that.mBuddyAddress))
      return false;
    if (!mBuddyNickname.equals(that.mBuddyNickname))
      return false;
    if (mType != that.mType)
      return false;
    return mGroup.equals(that.mGroup);
  }

  @Override
  public int hashCode()
  {
    int result = mBuddyAddress.hashCode();
    result = 31 * result + mBuddyNickname.hashCode();
    result = 31 * result + mType.hashCode();
    result = 31 * result + mGroup.hashCode();
    return result;
  }
}
