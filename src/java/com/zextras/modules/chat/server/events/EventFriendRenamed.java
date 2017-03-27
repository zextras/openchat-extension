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

import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.encoding.Encoder;
import com.zextras.modules.chat.server.encoding.EncoderFactory;
import com.zextras.modules.chat.server.interceptors.EventInterceptor;

public class EventFriendRenamed extends Event
{
  private final SpecificAddress mFriendToRename;
  private final String          mNewNickname;
  private final String          mNewGroup;

  public EventFriendRenamed(
    SpecificAddress sender,
    SpecificAddress friendToRename,
    String newNickname,
    String newGroup,
    Target target
  )
  {
    super(sender, target);
    mFriendToRename = friendToRename;
    mNewNickname = newNickname;
    mNewGroup = newGroup;
  }

  public SpecificAddress getFriendToRename()
  {
    return mFriendToRename;
  }

  public String getNewNickname()
  {
    return mNewNickname;
  }

  public String getNewGroup() { return mNewGroup; }

  @Override
  public <T> T interpret(EventInterpreter<T> interpreter)
  {
    return interpreter.interpret(this);
  }
}
