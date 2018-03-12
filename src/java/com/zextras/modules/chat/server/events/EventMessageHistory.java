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
import com.zextras.modules.chat.server.xmpp.encoders.EventMessageHistoryEncoder;

/**
 * @see EventMessageHistory
 * @see EventMessageHistoryEncoder
 * @see com.zextras.modules.chat.server.xmpp.parsers.MessageHistoryParser
 */
public class EventMessageHistory extends Event
{
  private final String mQueryId;
  private final SpecificAddress mMessageTo;
  private final Event mOriginalMessage;

  public EventMessageHistory(
    EventId eventId,
    ChatAddress sender,
    String queryId,
    SpecificAddress messageTo,
    Event originalMessage
  )
  {
    super(eventId,sender,new Target(messageTo));
    mQueryId = queryId;
    mMessageTo = messageTo;
    mOriginalMessage = originalMessage;
  }

  public EventMessageHistory(
    EventId eventId,
    ChatAddress sender,
    String queryId,
    SpecificAddress messageTo,
    Event originalMessage,
    long timestamp
  )
  {
    super(eventId,sender,new Target(messageTo),timestamp);
    mQueryId = queryId;
    mMessageTo = messageTo;
    mOriginalMessage = originalMessage;
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

  public Event getOriginalMessage()
  {
    return mOriginalMessage;
  }

  public String getQueryId()
  {
    return mQueryId;
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

    EventMessageHistory that = (EventMessageHistory) o;

    if (!mQueryId.equals(that.mQueryId))
      return false;
    if (!mMessageTo.equals(that.mMessageTo))
      return false;
    return mOriginalMessage.equals(that.mOriginalMessage);
  }

  @Override
  public int hashCode()
  {
    int result = super.hashCode();
    result = 31 * result + mQueryId.hashCode();
    result = 31 * result + mMessageTo.hashCode();
    result = 31 * result + mOriginalMessage.hashCode();
    return result;
  }
}

