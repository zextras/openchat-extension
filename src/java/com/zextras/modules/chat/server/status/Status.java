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

package com.zextras.modules.chat.server.status;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.utils.Jsonable;
import com.zextras.utils.ToJSONSerializer;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Status extends PartialStatus
{
  long validSince();
  List<SpecificAddress> meetings();

  boolean isInvisible();
  boolean isOffline();

  Status onlyMeeting(SpecificAddress address);
  Status withoutMeetings();

  @JsonSerialize(using = ToJSONSerializer.class)
  enum StatusType implements Jsonable
  {
    UNKNOWN(-1), OFFLINE(0),
    AVAILABLE(1), BUSY(2),
    AWAY(3), INVISIBLE(4),
    NEED_RESPONSE(5), INVITED(6),
    UNREACHABLE(7), UNSUBSCRIBED(8);

    private static final Map<Byte, StatusType> mByteToType = new HashMap<Byte, StatusType>();
    static {
      for (StatusType type : StatusType.values()) {
        mByteToType.put(type.toByte(), type);
      }
    }

    private final byte mTypeByte;

    StatusType(int typeByte)
    {
      mTypeByte = (byte) typeByte;
    }

    public byte toByte()
    {
      return mTypeByte;
    }

    public Byte toJSON()
    {
      return toByte();
    }

    public static StatusType fromByte(byte value)
    {
      StatusType type = mByteToType.get(value);
      return type != null ? type : UNKNOWN;
    }
  }

  StatusId getId();
}
