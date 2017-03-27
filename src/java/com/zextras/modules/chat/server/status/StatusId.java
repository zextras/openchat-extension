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

package com.zextras.modules.chat.server.status;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;


@JsonSerialize(using = ToStringSerializer.class)
public class StatusId implements Comparable<StatusId>
{
  private final Integer mId;

  public StatusId(int id)
  {
    mId = id;
  }

  public int id()
  {
    return mId;
  }

  @Override
  public int compareTo(StatusId o)
  {
    return mId.compareTo(o.id());
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

    StatusId statusId = (StatusId) o;

    if (!mId.equals(statusId.mId))
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

  @Override
  public String toString()
  {
    return mId.toString();
  }
}
