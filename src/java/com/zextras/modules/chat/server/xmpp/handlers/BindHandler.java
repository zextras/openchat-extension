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

import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.operations.XmppBindSession;
import com.zextras.modules.chat.server.xmpp.StanzaHandler;
import com.zextras.modules.chat.server.xmpp.XmppSession;
import com.zextras.modules.chat.server.xmpp.netty.StanzaProcessor;
import com.zextras.modules.chat.server.xmpp.parsers.BindParser;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

public class BindHandler implements StanzaHandler {
  private XmppSession mSession;
  private BindParser mParser;
  private StanzaProcessor.XmppConnectionHandler mXmppConnectionHandler;

  public BindHandler(XmppSession session) {
    mSession = session;
  }

  @Override
  public List<ChatOperation> handle() {
    return Arrays.<ChatOperation>asList(
      new XmppBindSession(
        mParser,
        mSession
      )
    );
  }

  @Override
  public void parse(ByteArrayInputStream xmlInputStream, SchemaProvider schemaProvider) throws XMLStreamException {
    mParser = new BindParser(xmlInputStream, schemaProvider);
    mParser.parse();
  }
}
