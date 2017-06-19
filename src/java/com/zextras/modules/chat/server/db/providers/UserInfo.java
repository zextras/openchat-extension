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

package com.zextras.modules.chat.server.db.providers;

import com.zextras.modules.chat.server.address.SpecificAddress;

public class UserInfo
{
  private final int             mId;
  
  private final SpecificAddress mAddress;
  private final boolean         mVaild;
  public UserInfo()
  {
    mId = -1;
    mAddress = null;
    mVaild = false;
  }
  
  public UserInfo(int id, SpecificAddress address)
  {
    mId = id;
    mAddress = address;
    mVaild = true;
  }
  
  public boolean isValid()
  {
    return mVaild;
  }
  
  public SpecificAddress getAddress()
  {
    return mAddress;
  }
  
  public int getId()
  {
    return mId;
  }
}
