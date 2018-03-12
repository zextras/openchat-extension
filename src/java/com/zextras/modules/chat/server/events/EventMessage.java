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

import com.zextras.modules.chat.server.exceptions.ChatException;
import org.openzal.zal.lib.Clock;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.SpecificAddress;

public class EventMessage extends Event
{
  private final SpecificAddress mSender;
  private final String          mMessage;
  private final TargetType      mType;

  public EventMessage(
    EventId eventId,
    SpecificAddress sender,
    Target target,
    String message
  )
  {
    super(eventId, sender, target);
    mSender = sender;
    mMessage = message;
    mType = TargetType.Chat;
  }

  public EventMessage(
    EventId eventId,
    SpecificAddress sender,
    Target target,
    String message,
    long timestamp,
    TargetType type
  )
  {
    super(eventId, sender, target, timestamp);
    mSender = sender;
    mMessage = message;
    mType = type;
  }

  public EventMessage(
    EventId eventId,
    SpecificAddress sender,
    Target target,
    String message,
    Clock clock
  )
  {
    super(eventId, sender, target, clock);
    mSender = sender;
    mMessage = message;
    mType = TargetType.Chat;
  }

  public EventMessage(
    EventId eventId,
    SpecificAddress sender,
    Target target,
    String message,
    long timestamp
  )
  {
    super(eventId, sender, target, timestamp);
    mSender = sender;
    mMessage = message;
    mType = TargetType.Chat;
  }

  public String getMessage()
  {
    return mMessage;
  }

  public SpecificAddress getSender()
  {
    return mSender;
  }

  @Override
  public <T> T interpret(EventInterpreter<T> interpreter) throws ChatException
  {
    return interpreter.interpret(this);
  }

  public TargetType getType()
  {
    return mType;
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

    EventMessage that = (EventMessage) o;

    if (!mSender.equals(that.mSender))
      return false;
    if (!mMessage.equals(that.mMessage))
      return false;
    return mType == that.mType;
  }

  @Override
  public int hashCode()
  {
    int result = super.hashCode();
    result = 31 * result + mSender.hashCode();
    result = 31 * result + mMessage.hashCode();
    result = 31 * result + mType.hashCode();
    return result;
  }

  public boolean isRoomEvent()
  {
    return mType != TargetType.Chat;
  }
}
