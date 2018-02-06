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

import com.zextras.modules.chat.server.*;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.status.Status;
import org.openzal.zal.lib.Clock;

public class EventStatusChanged extends Event
{
  public Status getStatus()
  {
    return mStatus;
  }

  @Override
  public SpecificAddress getSender() {
    return mSender;
  }

  private final SpecificAddress mSender;
  private final Status          mStatus;
  private final EventType       mType;

  public EventStatusChanged(SpecificAddress sender, Target target, Status status)
  {
    super(sender, target);
    mSender = sender;
    mStatus = status;
    mType = EventType.Chat;
  }

  public EventStatusChanged(EventId eventId, SpecificAddress sender, Target target, Status status, Clock clock)
  {
    super(eventId, sender, target, clock);
    mSender = sender;
    mStatus = status;
    mType = EventType.Chat;
  }

  public EventStatusChanged(SpecificAddress sender, Target target, Status status, EventType type)
  {
    super(sender, target);
    mSender = sender;
    mStatus = status;
    mType = type;
  }

  @Override
  public <T> T interpret(EventInterpreter<T> interpreter) throws ChatException
  {
    return interpreter.interpret(this);
  }

  @Override
  public String toString()
  {
    return "Event{" +
      getClass().getSimpleName() +
      ", mId=" + getId() +
      ", mType=" + mType +
      ", mSender=" + mSender.resourceAddress() +
      ", mTimestamp=" + getTimestamp() +
      ", mStatus=" + getStatus().getType().toString() +
      ", mTarget=" + getTarget().toString() +
      '}';
  }

  public EventType getType()
  {
    return mType;
  }

  public boolean isRoomEvent()
  {
    return mType != EventType.Chat;
  }
}
