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

import com.zextras.modules.chat.server.db.sql.ImMessageStatements;
import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.operations.History;
import com.zextras.modules.chat.server.xmpp.StanzaHandler;
import com.zextras.modules.chat.server.xmpp.netty.StanzaProcessor;
import com.zextras.modules.chat.server.xmpp.parsers.IQQueryXmppParser;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

public class IQQueryHandler implements StanzaHandler
{
  private final StanzaProcessor.XmppConnectionHandler mXmppConnectionHandler;
  private IQQueryXmppParser mParser = null;
  private ImMessageStatements mMessageStatements;

  public IQQueryHandler(
    StanzaProcessor.XmppConnectionHandler xmppConnectionHandler,
    ImMessageStatements messageStatements)
  {
    mXmppConnectionHandler = xmppConnectionHandler;
    mMessageStatements = messageStatements;
  }

  @Override
  public List<ChatOperation> handle()
  {
    return Arrays.<ChatOperation>asList(new History(
      mXmppConnectionHandler,
      mMessageStatements,
      mParser
    ));
  }

  @Override
  public void parse(ByteArrayInputStream xmlInputStream, SchemaProvider schemaProvider)
    throws XMLStreamException
  {
    mParser = new IQQueryXmppParser(xmlInputStream, schemaProvider);
    mParser.parse();
  }
}
