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

package com.zextras.modules.chat.server.xmpp.handlers;

import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.db.sql.ImMessageStatements;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.events.EventMessageHistory;
import com.zextras.modules.chat.server.events.EventMessageHistoryLast;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.operations.History;
import com.zextras.modules.chat.server.session.SessionManager;
import com.zextras.modules.chat.server.xmpp.StanzaHandler;
import com.zextras.modules.chat.server.xmpp.netty.StanzaProcessor;
import com.zextras.modules.chat.server.xmpp.parsers.IQQueryXmppParser;
import com.zextras.modules.chat.server.xmpp.parsers.MessageHistoryLastParser;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

public class MessageHistoryLastHandler implements StanzaHandler
{
  private MessageHistoryLastParser mParser = null;

  @Override
  public List<ChatOperation> handle()
  {
    return Arrays.<ChatOperation>asList(new ChatOperation()
    {
      @Override
      public List<Event> exec(SessionManager sessionManager, UserProvider userProvider) throws ChatException, ChatDbException
      {
        return Arrays.<Event>asList(new EventMessageHistoryLast(
          EventId.fromString(mParser.getId()),
          mParser.getQueryId(),
          new SpecificAddress(mParser.getTo()),
          mParser.getFirst(),
          mParser.getLast()
        ));
      }
    });
  }

  @Override
  public void parse(ByteArrayInputStream xmlInputStream, SchemaProvider schemaProvider)
    throws XMLStreamException
  {
    mParser = new MessageHistoryLastParser(xmlInputStream, schemaProvider);
    mParser.parse();
  }
}
