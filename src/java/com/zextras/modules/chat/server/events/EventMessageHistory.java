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
import com.zextras.modules.chat.server.address.NoneAddress;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.xmpp.encoders.EventMessageHistoryEncoder;
import org.openzal.zal.lib.ActualClock;
import org.openzal.zal.lib.Clock;

/**
 * @see EventMessageHistory
 * @see EventMessageHistoryEncoder
 * @see com.zextras.modules.chat.server.xmpp.parsers.MessageHistoryParser
 */
public class EventMessageHistory extends Event
{
  private final String mQueryId;
  private final SpecificAddress mMessageTo;
  private final EventMessage mOriginalMessage;

  public EventMessageHistory(
    EventId eventId,
    String queryId,
    SpecificAddress messageTo,
    EventMessage originalMessage
  )
  {
    super(eventId,new NoneAddress(),new Target(messageTo));
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

  public EventMessage getOriginalMessage()
  {
    return mOriginalMessage;
  }

  public String getQueryId()
  {
    return mQueryId;
  }
}

