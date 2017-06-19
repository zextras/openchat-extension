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

//import com.zextras.annotations.VisibleForTesting;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.encoding.Encoder;
import com.zextras.modules.chat.server.encoding.EncoderFactory;
import com.zextras.modules.chat.server.interceptors.EventInterceptor;

public class EventMessageAck extends Event
{
  public EventId getMessageId()
  {
    return mMessageId;
  }

  @Override
  public SpecificAddress getSender()
  {
    return mSender;
  }

  private final SpecificAddress mSender;
  private final EventId         mMessageId;

  public long getTimestamp()
  {
    return mTimestamp;
  }

  private final long mTimestamp;

  public EventMessageAck(SpecificAddress sender, SpecificAddress target, EventId messageId)
  {
    this(EventId.randomUUID(), sender, target, messageId, System.currentTimeMillis());
  }

  //@VisibleForTesting
  public EventMessageAck(EventId id, SpecificAddress sender, SpecificAddress target, EventId messageId, long timestamp)
  {
    super(id, sender, new Target(target));
    mSender = sender;
    mMessageId = messageId;
    mTimestamp = timestamp;
  }

  @Override
  public <T> T interpret(EventInterpreter<T> interpreter)
  {
    return interpreter.interpret(this);
  }
}
