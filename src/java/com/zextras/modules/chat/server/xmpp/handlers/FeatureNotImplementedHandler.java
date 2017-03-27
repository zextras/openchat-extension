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
import com.zextras.modules.chat.server.operations.FeatureNotImplementedAction;
import com.zextras.modules.chat.server.xmpp.StanzaHandler;
import com.zextras.modules.chat.server.xmpp.netty.StanzaProcessor;
import com.zextras.modules.chat.server.xmpp.parsers.FeatureNotImplementedParser;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;

public class FeatureNotImplementedHandler implements StanzaHandler
{
  private final StanzaProcessor.XmppConnectionHandler mConnectionStatus;
  private       FeatureNotImplementedParser           mParser;

  public FeatureNotImplementedHandler(StanzaProcessor.XmppConnectionHandler connectionStatus)
  {
    mConnectionStatus = connectionStatus;
  }

  @Override
  public List<ChatOperation> handle()
  {
    return Collections.<ChatOperation>singletonList(
      new FeatureNotImplementedAction(
        mConnectionStatus.getSession().getId(),
        mParser.getOriginalReceiver(),
        mParser.getRequestType(),
        mParser.getRequestId()
      )
    );
  }

  @Override
  public void parse(ByteArrayInputStream xmlInputStream, SchemaProvider schemaProvider) throws XMLStreamException
  {
    mParser = new FeatureNotImplementedParser(xmlInputStream, schemaProvider);
    mParser.parse();
  }
}
