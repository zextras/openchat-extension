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
import com.zextras.modules.chat.server.events.EventBindResult;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;


public class EventBindResultEncoder extends XmppEncoder {
  private final EventBindResult mEvent;

  public EventBindResultEncoder(EventBindResult event, SchemaProvider schemaProvider)
  {
    super("jabber-client.xsd", schemaProvider);
    mEvent = event;
  }

  /*
  <iq id='tn281v37' type='result'>
    <bind xmlns='urn:ietf:params:xml:ns:xmpp-bind'>
      <jid>
        juliet@im.example.com/4db06f06-1ea4-11dc-aca3-000bcd821bfb
      </jid>
    </bind>
   </iq>
   */

  @Override
  public void encode(OutputStream outputStream, SpecificAddress target)
    throws XMLStreamException
  {
    XMLStreamWriter2 sw = getStreamWriter(outputStream);
    sw.writeStartElement("iq");
    sw.writeAttribute("type", "result");
    sw.writeAttribute("id", mEvent.getId().toString());
    sw.writeAttribute("to",target.resourceAddress());
    sw.writeStartElement("", "bind", "urn:ietf:params:xml:ns:xmpp-bind");
    sw.writeStartElement("jid");
    String resourceAddress = target.resourceAddress();
    sw.writeCharacters(resourceAddress);
    sw.writeEndElement();
    sw.writeEndElement();
    sw.writeEndElement();
    sw.flush();
  }

}
