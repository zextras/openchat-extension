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
import com.zextras.modules.chat.server.events.EventXmppSASLAuthentication;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;


public class XmppSASLAuthResultEncoder extends XmppEncoder {
  private final EventXmppSASLAuthentication mEvent;

  public XmppSASLAuthResultEncoder(EventXmppSASLAuthentication event, SchemaProvider schemaProvider)
  {
    super("sasl.xsd", schemaProvider);
    mEvent = event;
  }

  @Override
  public void encode(OutputStream outputStream, SpecificAddress target)
    throws XMLStreamException
  {
    XMLStreamWriter2 sr = getStreamWriter(outputStream);
    if (validate())
    {
      sr.validateAgainst(getDefaultSchema());
    }

    switch (mEvent.getAuthStatus())
    {
      case SUCCESS:
        sr.writeEmptyElement("", "success", "urn:ietf:params:xml:ns:xmpp-sasl");
        break;

      case INVALID_CREDENTIALS:
        encodeFailure(sr, "not-authorized");
        break;

      case NOT_REQUESTED:
      case SERVER_ERROR:
        encodeFailure(sr, "temporary-auth-failure");
        break;

      case ACCOUNT_DISABLED:
        encodeFailure(sr, "account-disabled", "The account " + mEvent.getUsername() + " is currently disabled.");
    }

    sr.writeRaw("");
    sr.flush();
  }

  private void encodeFailure(XMLStreamWriter2 sr, String error) throws XMLStreamException {
    encodeFailure(sr, error, "");
  }

  private void encodeFailure(XMLStreamWriter2 sr, String error, String message) throws XMLStreamException {
    sr.writeStartElement("", "failure", "urn:ietf:params:xml:ns:xmpp-sasl");
    sr.writeEmptyElement("",error, "urn:ietf:params:xml:ns:xmpp-sasl" );

    if (!message.isEmpty()) {
      sr.writeStartElement("","text","urn:ietf:params:xml:ns:xmpp-sasl");
      sr.writeRaw(message);
      sr.writeEndElement();
    }
    sr.writeEndElement();
  }
}
