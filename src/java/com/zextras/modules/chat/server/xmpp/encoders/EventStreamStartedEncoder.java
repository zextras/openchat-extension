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
import com.zextras.modules.chat.server.events.EventStreamStarted;
import com.zextras.modules.chat.server.xmpp.XmppError;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

public class EventStreamStartedEncoder extends XmppEncoder
{
  private final EventStreamStarted mEvent;

  protected EventStreamStartedEncoder(EventStreamStarted event, SchemaProvider schemaProvider)
  {
    super("streams.xsd", schemaProvider);
    mEvent = event;
  }

/*
 <stream:stream
          from='im.example.com'
          id='++TR84Sm6A3hnt3Q065SnAbbk3Y='
          to='juliet@im.example.com'
          version='1.0'
          xml:lang='en'
          xmlns='jabber:client'
          xmlns:stream='http://etherx.jabber.org/streams'>
      <stream:error>
        <host-unknown
            xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>
      </stream:error>
 </stream:stream>
*/

  @Override
  public void encode(OutputStream outputStream, SpecificAddress target) throws XMLStreamException
  {
    XMLStreamWriter2 sw = getStreamWriter(outputStream);

    if( validate() ) {
      sw.validateAgainst(getDefaultSchema());
    }

    sw.writeStartDocument("utf-8", "1.0");
    sw.setPrefix("stream", "http://etherx.jabber.org/streams");
    sw.writeStartElement("stream", "stream", "http://etherx.jabber.org/streams");
    sw.writeAttribute("from", mEvent.getDomain());
    sw.writeAttribute("id", mEvent.getSessionId().toString());
    sw.writeAttribute("version", "1.0");
    sw.writeNamespace("", mEvent.getClientType().toXmpp());

    if( !mEvent.getXmppErrors().isEmpty() )
    {
      sw.writeStartElement("stream", "error", "http://etherx.jabber.org/streams");
      for( XmppError error : mEvent.getXmppErrors()) {
        encodeError(sw,error);
      }
      sw.writeEndElement();
      sw.writeEndElement();
    }
    else
    {
      sw.writeRaw("");
    }

    sw.flush();
  }

  private void encodeError(XMLStreamWriter2 encoder, XmppError error) throws XMLStreamException
  {
    switch (error)
    {
      case UnknownHost:
        encoder.writeEmptyElement("stream","host-unknown", "urn:ietf:params:xml:ns:xmpp-streams");
        break;

      default:
        throw new RuntimeException("invalid error");
    }

  }
}
