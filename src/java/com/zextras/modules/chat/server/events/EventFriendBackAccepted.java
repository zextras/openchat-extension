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

package com.zextras.modules.chat.server.events;


import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.encoding.Encoder;
import com.zextras.modules.chat.server.encoding.EncoderFactory;
import com.zextras.modules.chat.server.interceptors.EventInterceptor;

public class EventFriendBackAccepted extends Event
{
  public SpecificAddress getAcceptedFriend() {
    return mFriend;
  }

  private SpecificAddress mFriend;

  public EventFriendBackAccepted(SpecificAddress sender, SpecificAddress friend) {
    this(EventId.randomUUID(),sender,friend);
  }

  public EventFriendBackAccepted(EventId id, SpecificAddress sender, SpecificAddress friend) {
    super(id, sender, new Target(sender));
    mFriend = friend;
  }

  @Override
  public <T> T interpret(EventInterpreter<T> interpreter)
  {
    return interpreter.interpret(this);
  }
}
