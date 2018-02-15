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
import com.zextras.modules.chat.server.events.EventMessageBack;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

public class EventMessageBackEncoder extends XmppEncoder {

  private final EventMessageBack mEvent;

  protected EventMessageBackEncoder(EventMessageBack event, SchemaProvider schemaProvider) {
    super("jabber-client.xsd", schemaProvider);
    mEvent = event;
  }

  /*
  <message from='juliet@im.example.com/balcony'
              to='romeo@example.net'
              xml:lang='en'>
       <body>Art thou not Romeo, and a Montague?</body>
     </message>

   */
  @Override
  public void encode(OutputStream outputStream, SpecificAddress target, boolean extensions) throws XMLStreamException {
    XMLStreamWriter2 sw = getStreamWriter(outputStream);
//    if (validate()) {
//      sw.validateAgainst(getDefaultSchema());
//    }

    sw.writeStartElement("", "message", "jabber:client");
    sw.writeAttribute("from", mEvent.getSender().toString());
    sw.writeAttribute("to", target.resourceAddress());
    sw.writeAttribute("type", "chat");

//    if (validate()) {
//      sw.validateAgainst(getSchema("carbon.xsd"));
//    }
    sw.writeStartElement("", "sent", "urn:xmpp:carbons:2");
    sw.writeStartElement("", "forwarded", "urn:xmpp:forward:0");

    sw.writeStartElement("", "message", "jabber:client");
    sw.writeAttribute("to", mEvent.getMessageTo().toString());
    sw.writeAttribute("from", mEvent.getSender().resourceAddress());
    sw.writeAttribute("type", "chat");

    sw.writeStartElement("body");
    sw.writeCharacters(mEvent.getMessage());

    sw.writeEndElement();
    sw.writeEndElement();
    sw.writeEndElement();
    sw.writeEndElement();
    sw.writeEndElement();
    sw.close();
  }
}