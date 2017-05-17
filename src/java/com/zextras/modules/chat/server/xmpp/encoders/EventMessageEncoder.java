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
import com.zextras.modules.chat.server.events.EventMessage;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

public class EventMessageEncoder extends XmppEncoder
{
  private final EventMessage mEvent;

  protected EventMessageEncoder(EventMessage event, SchemaProvider schemaProvider)
  {
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
  public void encode(OutputStream outputStream, SpecificAddress target) throws XMLStreamException
  {
    XMLStreamWriter2 sw = getStreamWriter(outputStream);
    if( validate() ) {
      sw.validateAgainst(getDefaultSchema());
    }

    sw.writeStartElement("", "message", "jabber:client");
    sw.writeAttribute("from", mEvent.getSender().resourceAddress());
    sw.writeAttribute("to", target.resourceAddress());
    sw.writeAttribute("id", mEvent.getId().toString());

    sw.setPrefix("", "jabber:client");
    sw.writeStartElement("jabber:client", "body");
    sw.writeCharacters(mEvent.getMessage());
    sw.writeEndElement();
    sw.writeEndElement();

    sw.close();
  }
}
