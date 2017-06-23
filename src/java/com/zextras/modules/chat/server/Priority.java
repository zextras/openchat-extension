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

public class Priority implements Comparable<Priority>
{
  public static Priority HIGHEST_PRIORITY = new Priority(0);
  public static Priority LOWEST_PRIORITY = new Priority(Integer.MAX_VALUE);

  private final Integer mValue;

  public Priority(int value) {
    mValue = value;
  }

  public Integer getValue() {
    return mValue;
  }

  @Override
  public int compareTo(Priority o) {
    return mValue.compareTo(o.getValue());
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

    Priority priority = (Priority) o;

    if (!mValue.equals(priority.mValue))
    {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    return mValue.hashCode();
  }
}
