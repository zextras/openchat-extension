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

import com.zextras.modules.chat.server.Relationship;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.EventGetRelationships;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

public class EventRelationshipsEncoder extends XmppEncoder
{
  private final EventGetRelationships mEvent;

  protected EventRelationshipsEncoder(EventGetRelationships event, SchemaProvider schemaProvider)
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
/*
<iq id='bv1bs71f'
     to='juliet@example.com/chamber'
     type='result'>
  <query xmlns='jabber:iq:roster' ver='ver7'>
    <item jid='nurse@example.com' name="nickname1" subscription='both' />
    <item jid='romeo@example.net' name="nickname2" subscription='to' />
  </query>
</iq>
*/
    sw.writeStartElement("", "iq", "jabber:client");
    sw.writeAttribute("type", "result");
    sw.writeAttribute("id", mEvent.getId().toString());
    sw.writeAttribute("to", mEvent.getSender().resourceAddress());

    if( validate() ) {
      sw.validateAgainst(getSchema("roster.xsd"));
    }

    sw.writeStartElement("", "query", "jabber:iq:roster");
    sw.writeAttribute("ver","ver7");
    sw.setPrefix("", "jabber:iq:roster");
    for( Relationship rel : mEvent.getRelationships() )
    {
      sw.writeStartElement("jabber:iq:roster", "item");
      sw.writeAttribute("jid",rel.getBuddyAddress().toString());
      sw.writeAttribute("name",rel.getBuddyNickname());
      sw.writeAttribute("subscription", getSubscriptionType(rel) );

      if( rel.getType().equals(Relationship.RelationshipType.NEED_RESPONSE) ||
          rel.getType().equals(Relationship.RelationshipType.INVITED) )
      {
        sw.writeAttribute("ask","subscribe");
      }
      sw.writeStartElement("jabber:iq:roster","group");
      sw.writeCharacters(rel.getGroup());
      sw.writeEndElement();
      sw.writeEndElement();
    }
    sw.writeEndElement();
    sw.writeEndElement();
    sw.close();
  }

  public static String getSubscriptionType(Relationship rel)
  {
    switch( rel.getType() )
    {
      case ACCEPTED:
        return "both";

      case INVITED:
//        return "to";
        return "none";

      case NEED_RESPONSE:
        return "from";

      case BLOCKED:
        return "from";

      default:
        return "none";
    }
  }
}
