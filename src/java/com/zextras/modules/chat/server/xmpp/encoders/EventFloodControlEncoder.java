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

package com.zextras.modules.chat.server.xmpp.encoders;

import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.EventFloodControl;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

public class EventFloodControlEncoder extends XmppEncoder
{
  private final EventFloodControl mEvent;

  public EventFloodControlEncoder(EventFloodControl event, SchemaProvider schemaProvider)
  {
    super("jabber-client.xsd", schemaProvider);
    mEvent = event;
  }

  @Override
  public void encode(OutputStream outputStream, SpecificAddress target) throws XMLStreamException
  {
    XMLStreamWriter2 sw = getStreamWriter(outputStream);

    sw.writeStartElement("", "floodcontrol");
    sw.writeAttribute("flood_detected", String.valueOf(mEvent.isFloodDetected()));
    sw.writeAttribute("address", target.resourceAddress());
    sw.writeEndElement();

    sw.close();
  }
}
