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
import com.zextras.modules.chat.server.address.ChatAddress;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.exceptions.ChatException;

import javax.annotation.Nullable;

/**
 * @see EventMessageHistoryLast
 * @see com.zextras.modules.chat.server.xmpp.encoders.EventMessageHistoryLastEncoder
 * @see com.zextras.modules.chat.server.soap.encoders.EventMessageHistoryLastEncoder
 * @see com.zextras.modules.chat.server.xmpp.parsers.MessageHistoryLastParser
 */

public class EventMessageHistoryLast extends Event
{
  private final String mQueryId;
  private final SpecificAddress mMessageTo;
  private final String mFirstId;
  private final String mLastId;
  private final Long mCount;

  public EventMessageHistoryLast(
    EventId eventId,
    ChatAddress sender,
    String queryId,
    SpecificAddress messageTo,
    String firstId,
    String lastId
  )
  {
    this(eventId,sender,queryId,messageTo,firstId,lastId,(Long)null,0);
  }

  public EventMessageHistoryLast(
    EventId eventId,
    ChatAddress sender,
    String queryId,
    SpecificAddress messageTo,
    String firstId,
    String lastId,
    long count,
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

  @Nullable
  public Long getCount()
  {
    return mCount;
  }
}

