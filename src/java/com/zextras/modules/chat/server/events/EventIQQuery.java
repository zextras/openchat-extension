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

/**
 * @see com.zextras.modules.chat.server.xmpp.encoders.EventIQQueryEncoder
 * @see com.zextras.modules.chat.server.events.EventIQQuery
 * @see com.zextras.modules.chat.server.xmpp.parsers.IQQueryXmppParser
 * @see com.zextras.modules.chat.server.soap.command.SoapCommandQueryArchive
 */
public class EventIQQuery extends Event
{
  private String mQueryId;
  private String mNode;
  private String mWith;
  private String mStart;
  private String mEnd;

  public EventIQQuery(
    EventId eventId,
    SpecificAddress sender,
    String queryId,
    Target messageTo,
    String node,
    String with,
    String start,
    String end
  )
  {
    super(eventId,sender,messageTo);
    mQueryId = queryId;
    mNode = node;
    mWith = with;
    mStart = start;
    mEnd = end;
  }

  @Override
  public <T> T interpret(EventInterpreter<T> interpreter) throws ChatException
  {
    return interpreter.interpret(this);
  }

  public String getQueryId()
  {
    return mQueryId;
  }

  public String getNode()
  {
    return mNode;
  }

  public String getWith()
  {
    return mWith;
  }

  public String getStart()
  {
    return mStart;
  }

  public String getEnd()
  {
    return mEnd;
  }
}

