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
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.events.EventMessageHistory;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.session.SessionManager;
import com.zextras.modules.chat.server.xmpp.StanzaHandler;
import com.zextras.modules.chat.server.xmpp.parsers.MessageHistoryParser;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

public class MessageHistoryHandler implements StanzaHandler
{
  private MessageHistoryParser mParser = null;

  @Override
  public List<ChatOperation> handle()
  {
    return Arrays.<ChatOperation>asList(new ChatOperation()
    {
      @Override
      public List<Event> exec(SessionManager sessionManager, UserProvider userProvider) throws ChatException, ChatDbException
      {
        return Arrays.<Event>asList(new EventMessageHistory(
          EventId.fromString(mParser.getId()),
          new SpecificAddress(mParser.getSender()),
          mParser.getQueryId(),
          new SpecificAddress(mParser.getTo()),
          mParser.getEvent(),
          mParser.getTimestamp()
        ));
      }
    });
  }

  @Override
  public void parse(ByteArrayInputStream xmlInputStream, SchemaProvider schemaProvider)
    throws XMLStreamException
  {
    mParser = new MessageHistoryParser(xmlInputStream, schemaProvider);
    mParser.parse();
  }
}
