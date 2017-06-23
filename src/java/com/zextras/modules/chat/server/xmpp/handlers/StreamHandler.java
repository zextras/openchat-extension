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
import com.zextras.modules.chat.server.operations.SendXmppFeature;
import com.zextras.modules.chat.server.operations.XmppSessionRegistration;
import com.zextras.modules.chat.server.xmpp.StanzaHandler;
import com.zextras.modules.chat.server.xmpp.StanzaWriterFactory;
import com.zextras.modules.chat.server.xmpp.netty.StanzaProcessor;
import com.zextras.modules.chat.server.xmpp.parsers.StreamXmppParser;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.openzal.zal.Provisioning;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class StreamHandler implements StanzaHandler
{
  private final Provisioning                          mProvisioning;
  private final StanzaProcessor.XmppConnectionHandler mXmppConnectionHandler;
  private StreamXmppParser mStreamParser = null;
  private final StanzaWriterFactory mStanzaWriterFactory;

  public StreamHandler(
    Provisioning provisioning,
    StanzaProcessor.XmppConnectionHandler xmppConnectionHandler,
    StanzaWriterFactory stanzaWriterFactory
  )
  {
    mProvisioning = provisioning;
    mXmppConnectionHandler = xmppConnectionHandler;
    mStanzaWriterFactory = stanzaWriterFactory;
  }

  @Override
  public List<ChatOperation> handle()
  {
    List<ChatOperation> operations = new ArrayList<ChatOperation>(2);

    mXmppConnectionHandler.getSession().setDomain(mStreamParser.getDomain());

    XmppSessionRegistration registerEvent = new XmppSessionRegistration(
      mProvisioning,
      mXmppConnectionHandler,
      mStreamParser.getType(),
      mStreamParser.getDomain(),
      mStanzaWriterFactory
    );
    operations.add(registerEvent);

    mXmppConnectionHandler.getSession().setIsProxy(mStreamParser.isProxy());

    if( !mXmppConnectionHandler.getSession().isBindable() && !mStreamParser.isProxy() ) {
      SendXmppFeature featureEvent = new SendXmppFeature(
        mXmppConnectionHandler
      );
      operations.add(featureEvent);
    }

    return operations;
  }

  @Override
  public void parse(ByteArrayInputStream xmlInputStream, SchemaProvider schemaProvider) throws XMLStreamException
  {
    mStreamParser = new StreamXmppParser(xmlInputStream, schemaProvider);
    mStreamParser.parse();
  }
}
