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
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.interceptors.EventInterceptor;

public class EventFriendBackRemove extends Event
{
  public SpecificAddress getFriendToRemove() {
    return mFriendToRemove;
  }

  private final SpecificAddress mFriendToRemove;

  public EventFriendBackRemove(
      SpecificAddress sender,
      SpecificAddress friendToRemove
  )
  {
    this(EventId.randomUUID(),
         sender,
         friendToRemove);
  }

  public EventFriendBackRemove(EventId eventId, SpecificAddress sender, SpecificAddress friendToRemove) {
    super(eventId, sender, new Target(sender.withoutResource()));
    mFriendToRemove = friendToRemove;
  }

  @Override
  public <T> T interpret(EventInterpreter<T> interpreter) throws ChatException
  {
    return interpreter.interpret(this);
  }
}
