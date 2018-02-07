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
import com.zextras.modules.chat.server.events.EventXmppSessionEstablished;

import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

public class XmppSessionEstablishedEncoder extends XmppEncoder {

  private final EventXmppSessionEstablished mEvent;

  protected XmppSessionEstablishedEncoder(EventXmppSessionEstablished event, SchemaProvider schemaProvider)
  {
    super("jabber-client.xsd", schemaProvider);
    mEvent = event;
  }

  /*
    <iq from='example.com' type='result' id='sess_1'/>
    <iq type='result' id='62rq4-9'><session xmlns='urn:ietf:params:xml:ns:xmpp-session'/></iq>
   */

  @Override
  public void encode(OutputStream outputStream, SpecificAddress target, boolean extensions) throws XMLStreamException
  {
    XMLStreamWriter2 sr = getStreamWriter(outputStream);
    sr.writeStartElement("iq");
    sr.writeAttribute("from", mEvent.getDomain());
    sr.writeAttribute("id", mEvent.getId().toString());
    sr.writeAttribute("type", "result");
    sr.writeEmptyElement("","session","urn:ietf:params:xml:ns:xmpp-session");
    sr.writeEndElement();
    sr.flush();
  }
}
