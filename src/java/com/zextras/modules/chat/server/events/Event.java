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
import org.openzal.zal.lib.ActualClock;
import org.openzal.zal.lib.Clock;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.ChatAddress;
import com.zextras.modules.chat.server.address.SpecificAddress;

public abstract class Event
{
  private final EventId     mId;
  private final long        mTimestamp;
  private final ChatAddress mSender;
  private final Target      mTarget;

  protected Event(ChatAddress sender, Target target)
  {
    this(EventId.randomUUID(), sender, target);
  }

  protected Event(EventId id, ChatAddress sender, Target target)
  {
    mId = id;
    mSender = sender;
    mTarget = target;
    mTimestamp = ActualClock.sInstance.now();
  }

  protected Event(EventId id, ChatAddress sender, Target target, long timestamp)
  {
    mId = id;
    mSender = sender;
    mTarget = target;
    mTimestamp = timestamp;
  }

  //VisibleForTesting
  public Event(EventId id, SpecificAddress sender, Target target, Clock clock) {
    mId = id;
    mSender = sender;
    mTarget = target;
    mTimestamp = clock.now();
  }

  public EventId getId()
  {
    return mId;
  }

  public Target getTarget()
  {
    return mTarget;
  }

  public long getTimestamp()
  {
    return mTimestamp;
  }

  public ChatAddress getSender()
  {
    return mSender;
  }

  public abstract <T> T interpret(EventInterpreter<T> interpreter) throws ChatException;

  @Override
  public boolean equals(Object object)
  {
    if(!(object instanceof Event)) {
      return false;
    }
    Event event = (Event) object;
    return mId.equals(event.mId);
  }

  @Override
  public int hashCode()
  {
    return mId.hashCode();
  }

  @Override
  public String toString() {
    return "Event{" +
        getClass().getSimpleName() +
        ", id=" + mId +
        ", sender=" + mSender.resourceAddress() +
        ", timestamp=" + mTimestamp +
        ", target=" + mTarget.toString() +
        '}';
  }
}
