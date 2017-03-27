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
import com.zextras.modules.chat.server.events.EventFriendAdded;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

public class EventFriendAddedEncoder extends XmppEncoder
{
  private final EventFriendAdded mEvent;

  public EventFriendAddedEncoder(EventFriendAdded event, SchemaProvider schemaProvider)
  {
    super("jabber-client.xsd", schemaProvider);
    mEvent = event;
  }

  /*
    <iq id='a78b4q6ha463'
         to='juliet@example.com/chamber'
         type='set'>
      <query xmlns='jabber:iq:roster'>
        <item jid='nurse@example.com'/>
      </query>
    </iq>

        sw.writeStartElement("","iq","jabber:client");
      sw.writeAttribute("to",mEvent.getTarget().resourceAddress());
      sw.writeAttribute("type","set");
      sw.writeAttribute("id",mEvent.getId().toString());
      sw.writeAttribute("from",mEvent.getSender().toString());

      if( validate() ) {
        sw.validateAgainst(getSchema("roster.xsd"));
      }

      sw.writeStartElement("","query","jabber:iq:roster");
      sw.writeEmptyElement("","item","jabber:iq:roster");
      sw.writeAttribute("jid",mEvent.getTarget().toString());
      sw.writeAttribute("subscription", "from" );
      sw.writeEndElement();
      sw.writeEndElement();
  */
  @Override
  public void encode(OutputStream outputStream, SpecificAddress target) throws XMLStreamException
  {
    XMLStreamWriter2 sw = getStreamWriter(outputStream);

    if( validate() ) {
      sw.validateAgainst(getDefaultSchema());
    }

    sw.writeEmptyElement("","presence","jabber:client");
    sw.writeAttribute("id", mEvent.getId().toString());
    sw.writeAttribute("from", mEvent.getSender().resourceAddress());
    sw.writeAttribute("to", target.resourceAddress());
    sw.writeAttribute("type", "subscribe");

    sw.close();
  }
}
