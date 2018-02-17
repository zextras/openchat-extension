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
import com.zextras.modules.chat.server.operations.QueryArchiveFactory;
import com.zextras.modules.chat.server.xmpp.StanzaHandler;
import com.zextras.modules.chat.server.xmpp.XmppSession;
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
  private final QueryArchiveFactory mQueryArchiveFactory;
  private IQQueryXmppParser mParser = null;

  public IQQueryHandler(
    StanzaProcessor.XmppConnectionHandler xmppConnectionHandler,
    QueryArchiveFactory queryArchiveFactory
  )
  {
    mXmppConnectionHandler = xmppConnectionHandler;
    mQueryArchiveFactory = queryArchiveFactory;
  }

  @Override
  public List<ChatOperation> handle()
  {
    XmppSession session = mXmppConnectionHandler.getSession();

    return Arrays.<ChatOperation>asList(mQueryArchiveFactory.create(
      session.getMainAddress(),
      mParser.getQueryId(),
      mParser.getWith(),
      mParser.getStart(),
      mParser.getEnd(),
      mParser.getNode(),
      mParser.getMax()
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
