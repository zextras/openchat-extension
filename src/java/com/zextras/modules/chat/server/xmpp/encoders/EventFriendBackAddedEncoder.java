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
import com.zextras.modules.chat.server.events.EventFriendBackAdded;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

public class EventFriendBackAddedEncoder extends XmppEncoder {
  private final EventFriendBackAdded mEvent;

  public EventFriendBackAddedEncoder(EventFriendBackAdded eventBackFriendAdded, SchemaProvider schemaProvider)
  {
    super("jabber-client.xsd", schemaProvider);
    mEvent = eventBackFriendAdded;
  }

/*
  <iq id="push287551665" to="test@example.com/First" from="test@example.com" type="set">
    <query xmlns="jabber:iq:roster">
      <item jid="friend@example.com" name="friend" subscription="none" />
    </query>
  </iq>
*/

  @Override
  public void encode(OutputStream outputStream, SpecificAddress target)
      throws XMLStreamException
  {
    XMLStreamWriter2 sw = getStreamWriter(outputStream);

    if( validate() ) {
      sw.validateAgainst(getDefaultSchema());
    }

    sw.writeStartElement("", "iq", "jabber:client");
    sw.writeAttribute("id", mEvent.getId().toString());
    sw.writeAttribute("to", target.resourceAddress());
    sw.writeAttribute("from", mEvent.getSender().toString());
    sw.writeAttribute("type", "set");

    if( validate() ) {
      sw.validateAgainst(getSchema("roster.xsd"));
    }

    sw.writeStartElement("", "query", "jabber:iq:roster");
    sw.writeEmptyElement("", "item", "jabber:iq:roster");
    sw.writeAttribute("jid", mEvent.getFriendToAdd().toString());
    sw.writeAttribute("name", mEvent.getNickname());
    sw.writeAttribute("subscription", "none");
    sw.writeAttribute("ask", "subscribe");


//    sw.writeStartElement("jabber:iq:roster", "group");
//    sw.writeCharacters("zimbra");
//    sw.writeEndElement();

    sw.writeEndElement();
    sw.writeEndElement();

    sw.close();
  }
}
