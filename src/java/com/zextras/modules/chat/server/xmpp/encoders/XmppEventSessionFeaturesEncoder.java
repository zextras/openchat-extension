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
import com.zextras.modules.chat.server.events.EventXmppSessionFeatures;
import com.zextras.modules.chat.server.xmpp.XmppAuthentication;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

public class XmppEventSessionFeaturesEncoder extends XmppEncoder
{
  private final EventXmppSessionFeatures mEvent;

  protected XmppEventSessionFeaturesEncoder(
    EventXmppSessionFeatures event,
    SchemaProvider schemaProvider
  )
  {
    super("streams.xsd", schemaProvider);
    mEvent = event;
  }

  @Override
  public void encode(OutputStream outputStream, SpecificAddress target, boolean extensions) throws XMLStreamException
  {
    XMLStreamWriter2 sw = getStreamWriter(outputStream);

    if( validate() ) {
      sw.validateAgainst(getDefaultSchema());
    }

    sw.writeStartElement("stream", "features", "http://etherx.jabber.org/streams");

    if (!mEvent.getAvailableAuthentications().isEmpty())
    {
      sw.setPrefix("", "urn:ietf:params:xml:ns:xmpp-sasl");
      sw.writeStartElement("urn:ietf:params:xml:ns:xmpp-sasl", "mechanisms");
      sw.writeDefaultNamespace("urn:ietf:params:xml:ns:xmpp-sasl");

      for( XmppAuthentication mechanism : mEvent.getAvailableAuthentications() )
      {
        sw.writeStartElement("urn:ietf:params:xml:ns:xmpp-sasl", "mechanism");
        sw.writeCharacters(mechanism.toString());
        sw.writeEndElement();
      }
      sw.writeEndElement();
    }

    if (mEvent.isBindable()) {
      sw.writeEmptyElement("", "session", "urn:ietf:params:xml:ns:xmpp-session");
      sw.writeEmptyElement("", "bind", "urn:ietf:params:xml:ns:xmpp-bind");
    }

    if(!mEvent.isUsingSSL()) {
      sw.writeStartElement("", "starttls", "urn:ietf:params:xml:ns:xmpp-tls");
      if( mEvent.sslRequired() ) {
        sw.writeEmptyElement("", "required", "urn:ietf:params:xml:ns:xmpp-tls");
      }
      sw.writeEndElement();
    }

    sw.writeEndElement();
    sw.close();
  }
}
