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

package com.zextras.modules.chat.server.xmpp;

import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.xmpp.netty.StanzaProcessor;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

public class StartTLSHandler implements StanzaHandler
{
  private final StanzaProcessor.XmppConnectionHandler mConnectionStatus;

  public StartTLSHandler(StanzaProcessor.XmppConnectionHandler connectionStatus) {
    mConnectionStatus = connectionStatus;
  }

  @Override
  public List<ChatOperation> handle()
  {
    if( mConnectionStatus.getSession().isUsingSSL() )
    {
      ChatLog.log.info("Requested TLS initialization when TLS is already initialized");
    }
    else
    {
      try
      {
        ByteBuf buffer = Unpooled.copiedBuffer("<proceed xmlns=\"urn:ietf:params:xml:ns:xmpp-tls\"/>".getBytes("UTF-8"));
        mConnectionStatus.startTLS(buffer);
      }
      catch (UnsupportedEncodingException ex) {
        throw new RuntimeException();
      }
    }

    return Collections.emptyList();
  }

  @Override
  public void parse(ByteArrayInputStream xmlInputStream, SchemaProvider schemaProvider) throws XMLStreamException
  {
  }
}
