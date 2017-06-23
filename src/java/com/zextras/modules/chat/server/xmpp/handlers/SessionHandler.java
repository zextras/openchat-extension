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
import com.zextras.modules.chat.server.operations.XmppSendSessionEstablished;
import com.zextras.modules.chat.server.xmpp.StanzaHandler;
import com.zextras.modules.chat.server.xmpp.StanzaWriterFactory;
import com.zextras.modules.chat.server.xmpp.netty.StanzaProcessor;
import com.zextras.modules.chat.server.xmpp.parsers.SessionParser;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.openzal.zal.Provisioning;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;


public class SessionHandler implements StanzaHandler {
  private final StanzaProcessor.XmppConnectionHandler mXmppConnectionHandler;
  private       Provisioning                          mProvisioning;
  private       StanzaWriterFactory                   mStanzaWriterFactory;
  private       SessionParser                         mParser;

  public SessionHandler(StanzaProcessor.XmppConnectionHandler xmppConnectionHandler,
                        Provisioning provisioning,
                        StanzaWriterFactory stanzaWriterFactory
  )
  {
    mXmppConnectionHandler = xmppConnectionHandler;
    mProvisioning = provisioning;
    mStanzaWriterFactory = stanzaWriterFactory;
  }

  @Override
  public List<ChatOperation> handle()
  {
    return Arrays.<ChatOperation>asList(
      new XmppSendSessionEstablished(
        mParser.getEventId(),
        mXmppConnectionHandler.getSession()
      )
    );
  }

  @Override
  public void parse(ByteArrayInputStream xmlInputStream, SchemaProvider schemaProvider)
    throws XMLStreamException
  {
    mParser = new SessionParser(xmlInputStream, schemaProvider);
    mParser.parse();
  }
}
