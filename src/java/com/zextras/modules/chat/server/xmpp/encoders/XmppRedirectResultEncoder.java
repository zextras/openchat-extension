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
import com.zextras.modules.chat.server.events.EventXmppRedirect;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

/**
 * zextras
 * User: marco
 * Date: 13/11/13 15.20
 */
public class XmppRedirectResultEncoder extends XmppEncoder {
  private final EventXmppRedirect mEvent;

  public XmppRedirectResultEncoder(EventXmppRedirect event, SchemaProvider schemaProvider)
  {
    super("streams.xsd", schemaProvider);
    mEvent = event;
  }

  @Override
  public void encode(OutputStream outputStream, SpecificAddress target)
    throws XMLStreamException
  {
    XMLStreamWriter2 sr = getStreamWriter(outputStream);
//    if (validate()) {
//      sr.validateAgainst(getDefaultSchema());
//    }

    sr.writeStartElement("stream", "error", "jabber:client");

    if (validate()) {
      sr.validateAgainst(getSchema("streamerror.xsd"));
    }

    sr.writeStartElement("", "see-other-host", "urn:ietf:params:xml:ns:xmpp-streams");
    sr.writeCharacters(mEvent.getRemoteHost());
    sr.writeEndElement();
    sr.writeEndElement();
    sr.flush();
  }
}
