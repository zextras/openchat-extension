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
import com.zextras.modules.chat.server.exceptions.ChatException;

// TODO: it's not compatible wit older code

/**
 * This event is cross server.
 */
public class EventMessageAck extends Event
{
  @Override
  public SpecificAddress getSender()
  {
    return mSender;
  }

  private final SpecificAddress mSender;
  private final EventId         mMessageId;
  private final long            mMessageTimestamp;

  public EventMessageAck(SpecificAddress sender, SpecificAddress target, EventId messageId, long timestamp)
  {
    this(EventId.randomUUID(), sender, target, messageId, timestamp);
  }

  public EventMessageAck(EventId id, SpecificAddress sender, SpecificAddress target, EventId messageId, long messageTimestamp)
  {
    super(id, sender, new Target(target));
    mSender = sender;
    mMessageId = messageId;
    mMessageTimestamp = messageTimestamp;
  }

  @Override
  public <T> T interpret(EventInterpreter<T> interpreter) throws ChatException
  {
    return interpreter.interpret(this);
  }

  public long getMessageTimestamp()
  {
    return mMessageTimestamp;
  }

  public EventId getMessageId()
  {
    return mMessageId;
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    if (!super.equals(o))
      return false;

    EventMessageAck that = (EventMessageAck) o;

    if (mMessageTimestamp != that.mMessageTimestamp)
      return false;
    if (!mSender.equals(that.mSender))
      return false;
    return mMessageId.equals(that.mMessageId);
  }

  @Override
  public int hashCode()
  {
    int result = super.hashCode();
    result = 31 * result + mSender.hashCode();
    result = 31 * result + mMessageId.hashCode();
    result = 31 * result + (int) (mMessageTimestamp ^ (mMessageTimestamp >>> 32));
    return result;
  }
}
