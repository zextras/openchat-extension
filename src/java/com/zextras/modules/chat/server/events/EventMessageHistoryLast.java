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

import com.zextras.lib.Optional;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.ChatAddress;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.exceptions.ChatException;

/**
 * @see EventMessageHistoryLast
 * @see com.zextras.modules.chat.server.xmpp.encoders.EventMessageHistoryLastEncoder
 * @see com.zextras.modules.chat.server.soap.encoders.EventMessageHistoryLastEncoder
 * @see com.zextras.modules.chat.server.xmpp.parsers.MessageHistoryLastParser
 */

public class EventMessageHistoryLast extends Event
{
  private final String            mQueryId;
  private final SpecificAddress   mMessageTo;
  private final String            mFirstId;
  private final String            mLastId;
  private final Optional<Integer> mCount;

  public EventMessageHistoryLast(
    EventId eventId,
    ChatAddress sender,
    String queryId,
    SpecificAddress messageTo,
    String firstId,
    String lastId,
    Optional<Integer> count,
    long timestamp
  )
  {
    super(eventId,sender,new Target(messageTo),timestamp);
    mQueryId = queryId;
    mMessageTo = messageTo;
    mFirstId = firstId;
    mLastId = lastId;
    mCount = count;
  }

  @Override
  public <T> T interpret(EventInterpreter<T> interpreter) throws ChatException
  {
    return interpreter.interpret(this);
  }

  public SpecificAddress getMessageTo()
  {
    return mMessageTo;
  }

  public String getQueryId()
  {
    return mQueryId;
  }

  public String getFirstId()
  {
    return mFirstId;
  }

  public String getLastId()
  {
    return mLastId;
  }

  public Optional<Integer> getCount()
  {
    return mCount;
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

    EventMessageHistoryLast that = (EventMessageHistoryLast) o;

    if (!mQueryId.equals(that.mQueryId))
      return false;
    if (!mMessageTo.equals(that.mMessageTo))
      return false;
    if (!mFirstId.equals(that.mFirstId))
      return false;
    if (!mLastId.equals(that.mLastId))
      return false;
    return mCount.equals(that.mCount);
  }

  @Override
  public int hashCode()
  {
    int result = super.hashCode();
    result = 31 * result + mQueryId.hashCode();
    result = 31 * result + mMessageTo.hashCode();
    result = 31 * result + mFirstId.hashCode();
    result = 31 * result + mLastId.hashCode();
    result = 31 * result + mCount.hashCode();
    return result;
  }
}

