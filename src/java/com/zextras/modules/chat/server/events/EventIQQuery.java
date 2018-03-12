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
 * @see com.zextras.modules.chat.server.xmpp.encoders.EventIQQueryEncoder
 * @see com.zextras.modules.chat.server.events.EventIQQuery
 * @see com.zextras.modules.chat.server.xmpp.parsers.IQQueryXmppParser
 * @see com.zextras.modules.chat.server.soap.command.SoapCommandQueryArchive
 */
public class EventIQQuery extends Event
{
  private final SpecificAddress mSender;
  private final String mQueryId;
  private final Optional<String> mNode;
  private final Optional<String> mWith;
  private final Optional<Long> mStart;
  private final Optional<Long> mEnd;
  private final Optional<Integer> mMax;

  public EventIQQuery(
    EventId eventId,
    SpecificAddress sender,
    String queryId,
    Target messageTo,
    Optional<String> node,
    Optional<String> with,
    Optional<Long> start,
    Optional<Long> end,
    Optional<Integer> max
  )
  {
    super(eventId,sender,messageTo);
    mSender = sender;
    mQueryId = queryId;
    mNode = node;
    mWith = with;
    mStart = start;
    mEnd = end;
    mMax = max;
  }

  @Override
  public <T> T interpret(EventInterpreter<T> interpreter) throws ChatException
  {
    return interpreter.interpret(this);
  }

  @Override
  public SpecificAddress getSender()
  {
    return mSender;
  }

  public String getQueryId()
  {
    return mQueryId;
  }

  public Optional<String> getNode()
  {
    return mNode;
  }

  public Optional<String> getWith()
  {
    return mWith;
  }

  public Optional<Long> getStart()
  {
    return mStart;
  }

  public Optional<Long> getEnd()
  {
    return mEnd;
  }

  public Optional<Integer> getMax()
  {
    return mMax;
  }

  @Override
  public String toString() {
    return "Event{" +
      getClass().getSimpleName() +
      ", id=" + getId() +
      ", sender=" + mSender.resourceAddress() +
      ", timestamp=" + getTimestamp() +
      ", target=" + getTarget().toString() +
      ", queryId=" + getQueryId() +
      ", node=" + getNode() +
      ", with=" + getWith() +
      ", start=" + getStart() +
      ", end=" + getEnd() +
      ", max=" + getMax() +
      '}';
  }
}

