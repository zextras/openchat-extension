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

package com.zextras.modules.chat.server.events;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.zextras.utils.FastBase64;
import io.netty.util.internal.ThreadLocalRandom;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@SuppressWarnings("CallToStringCompareTo")
@JsonSerialize(using = ToStringSerializer.class)
public class EventId implements Comparable<EventId>
{
  private final String mId;

  public EventId(UUID uuid) {
    this(uuid.toString());
  }

  public EventId(String id) {
    mId = id;
  }

  public static EventId randomUUID() {
    byte[] randombytes = new byte[ 3 ];
    ThreadLocalRandom random = ThreadLocalRandom.current();
    random.nextBytes(randombytes);
    return new EventId(
      FastBase64.encodeToString(randombytes, false)
    );
  }

  public static EventId fromString( @NotNull String s) {
    if( s.isEmpty() ) {
      throw new RuntimeException("Invalid empty id");
    }
    return new EventId(s);
  }

  @Override
  public int compareTo(@NotNull EventId uid) {
    return mId.compareTo(uid.mId);
  }

  @Override
  public String toString(){
    return mId;
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

    EventId eventId = (EventId) o;

    if (!mId.equals(eventId.mId))
    {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    return mId.hashCode();
  }
}
