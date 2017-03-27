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
import com.zextras.modules.chat.server.events.FriendNotFoundEvent;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

public class EventFriendNotFoundEncoder extends XmppEncoder
{
  private final FriendNotFoundEvent mEvent;

  public EventFriendNotFoundEncoder(FriendNotFoundEvent event, SchemaProvider schemaProvider)
  {
    super("jabber-client.xsd", schemaProvider);
    mEvent = event;
  }

  @Override
  public void encode(OutputStream outputStream, SpecificAddress target) throws XMLStreamException
  {
    XMLStreamWriter2 sw = getStreamWriter(outputStream);

    if (validate())
    {
      sw.validateAgainst(getDefaultSchema());
    }

    sw.writeStartElement("","presence","jabber:client");
    sw.writeAttribute("type", "error");

    sw.writeAttribute("id", mEvent.getId().toString());

    sw.writeAttribute("from", mEvent.getFrientToAdd().toString());

    sw.writeAttribute("to", mEvent.getSender().toString());

    sw.writeStartElement("","error","jabber:client");

    sw.writeAttribute("type","modify");

    sw.writeEmptyElement("","recipient-unavailable", "urn:ietf:params:xml:ns:xmpp-stanzas");

    sw.writeEndElement();

    sw.writeEndElement();

    sw.close();
  }
}
