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

package com.zextras.modules.chat.server.xmpp.encoders;

import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.EventXmppPing;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

/**
 * zextras
 * User: marco
 * Date: 12/11/13 16.34
 */
public class PingEncoder extends XmppEncoder
{
  private final EventXmppPing mEvent;

  public PingEncoder(EventXmppPing event, SchemaProvider schemaProvider)
  {
    super("ping.xsd", schemaProvider);
    mEvent = event;
  }

  // <iq from='juliet@capulet.lit/balcony' to='capulet.lit' id='s2c1' type='result'/>

  @Override
  public void encode(OutputStream outputStream, SpecificAddress target) throws XMLStreamException
  {
    XMLStreamWriter2 sr = getStreamWriter(outputStream);
    sr.writeStartElement("iq");

    sr.writeAttribute("from", mEvent.getSender().resourceAddress());
    sr.writeAttribute("to",  target.resourceAddress());

    sr.writeAttribute("id", mEvent.getIQId());
    sr.writeAttribute("type", "result");
    sr.writeEndElement();
    sr.flush();
  }
}
