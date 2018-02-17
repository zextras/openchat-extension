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
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.operations.SendMessageAck;
import com.zextras.modules.chat.server.xmpp.StanzaHandler;
import com.zextras.modules.chat.server.xmpp.XmppSession;
import com.zextras.modules.chat.server.xmpp.parsers.MessageAckParser;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;

public class MessageAckHandler implements StanzaHandler
{
  private final XmppSession      mSession;
  private       MessageAckParser mParser;

  public MessageAckHandler(XmppSession session)
  {
    mSession = session;
  }

  public List<ChatOperation> handleMessageAck()
  {
    SpecificAddress target = new SpecificAddress(mParser.getTo());
    EventId messageId = EventId.fromString(mParser.getMessageId());

    return Collections.<ChatOperation>singletonList(
      new SendMessageAck(
        mSession.getExposedAddress(),
        target,
        messageId,
        System.currentTimeMillis(),
        mSession.getId()
      )
    );
  }

  @Override
  public List<ChatOperation> handle()
  {
    return handleMessageAck();
  }

  @Override
  public void parse(ByteArrayInputStream xmlInputStream, SchemaProvider schemaProvider) throws XMLStreamException
  {
    mParser = new MessageAckParser(xmlInputStream, schemaProvider);
    mParser.parse();
  }

  public void setParser(MessageAckParser parser)
  {
    mParser = parser;
  }
}
