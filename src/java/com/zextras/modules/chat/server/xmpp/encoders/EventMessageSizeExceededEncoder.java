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
import com.zextras.modules.chat.server.events.EventMessageSizeExceeded;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;


public class EventMessageSizeExceededEncoder extends XmppEncoder
{
  private final EventMessageSizeExceeded mEvent;

  public EventMessageSizeExceededEncoder
    (
      EventMessageSizeExceeded eventMessageSizeExceeded,
      SchemaProvider schemaProvider
    )
  {
    super("jabber-client.xsd", schemaProvider);
    mEvent = eventMessageSizeExceeded;
  }

  /*
  <message from='shakespeare.lit' to='iago@shakespare.lit/evilos'>
  <error type='modify'>
    <not-allowed xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>
    <stanza-too-big xmlns='urn:xmpp:errors'/>
  </error>
  </message>
   */

  @Override
  public void encode(OutputStream outputStream, SpecificAddress target)
    throws XMLStreamException
  {
    XMLStreamWriter2 sw = getStreamWriter(outputStream);

//    if( validate() )
//    {
//      sw.validateAgainst(getDefaultSchema());
//    }

    sw.writeStartElement("", "message", "jabber:client");
    sw.writeAttribute("from", mEvent.getSender().resourceAddress());
    sw.writeAttribute("to", target.resourceAddress());
    sw.writeAttribute("id", mEvent.getId().toString());

    sw.writeStartElement("", "error", "jabber:client");
    sw.writeAttribute("code", "406");
    sw.writeAttribute("type", "modify");
    sw.writeEmptyElement("", "not-acceptable", "urn:ietf:params:xml:ns:xmpp-stanzas");
    sw.writeEmptyElement("", "payload-too-big", "http://jabber.org/protocol/pubsub#errors");
    sw.writeEndElement();

    sw.writeEndElement();
    sw.close();
  }
}
