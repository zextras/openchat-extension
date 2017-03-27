/*
 * ZAL - The abstraction layer for Zimbra.
 * Copyright (C) 2017 ZeXtras S.r.l.
 *
 * This file is part of ZAL.
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
 * You should have received a copy of the GNU General Public License
 * along with ZAL. If not, see <http://www.gnu.org/licenses/>.
 */

package com.zextras.modules.chat.server.xmpp.handlers;

import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.operations.XmppPing;
import com.zextras.modules.chat.server.xmpp.StanzaHandler;
import com.zextras.modules.chat.server.xmpp.XmppSession;
import com.zextras.modules.chat.server.xmpp.parsers.PingParser;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;


public class PingHandler implements StanzaHandler {
  private XmppSession mSession;
  private PingParser mParser;

  public PingHandler(XmppSession session) {
    mSession = session;
  }

  @Override
  public List<ChatOperation> handle() {
    String iqId = mParser.getIQId();
    if (iqId.isEmpty()) {
      iqId = EventId.randomUUID().toString();
    }

    return Arrays.<ChatOperation>asList(
      new XmppPing(
        EventId.fromString(iqId),
        mSession.getExposedAddress().withoutSession()
      )
    );
  }

  @Override
  public void parse(ByteArrayInputStream xmlInputStream, SchemaProvider schemaProvider) throws XMLStreamException {
    mParser = new PingParser(xmlInputStream, schemaProvider);
    mParser.parse();
  }
}
