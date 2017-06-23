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

package com.zextras.modules.chat.server.session;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.util.UUID;

@JsonSerialize(using = ToStringSerializer.class)
public class SessionUUID implements Comparable<SessionUUID>
{
  @JsonProperty
  private UUID mUuid;

  public SessionUUID(UUID uuid)
  {
    mUuid = uuid;
  }

  public static SessionUUID randomUUID()
  {
    UUID uuid = UUID.randomUUID();
    return new SessionUUID(uuid);
  }

  public static SessionUUID fromString(String s) {
    UUID uuid = UUID.fromString(s);
    return new SessionUUID(uuid);
  }

  @Override
  public int compareTo(SessionUUID uid) {
    return mUuid.compareTo(uid.mUuid);
  }

  @Override
  public String toString(){
    return mUuid.toString();
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

    SessionUUID that = (SessionUUID) o;

    if (!mUuid.equals(that.mUuid))
    {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    return mUuid.hashCode();
  }
}
