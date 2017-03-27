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

import com.zextras.modules.chat.server.status.Status;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.EventStatusChanged;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

public class EventStatusChangedEncoder extends XmppEncoder
{
  private final EventStatusChanged mEvent;

  protected EventStatusChangedEncoder(EventStatusChanged event, SchemaProvider schemaProvider)
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
<presence from='romeo@example.net/orchard' xml:lang='en'>
  <show>dnd</show>
  <status>Wooing Juliet</status>
  <status xml:lang='cs'>Dvo&#x0159;&#x00ED;m se Julii</status>
</presence>

online: chat
away: away
busy: dnd
*/
    sw.writeStartElement("", "presence", "jabber:client");
    // sw.writeAttribute("id", mEvent.getId().toString());
    sw.writeAttribute("to", target.withoutSession().toString());

    Status status = mEvent.getStatus();
    if(   status.getType().equals(Status.StatusType.INVISIBLE)
       || status.getType().equals(Status.StatusType.OFFLINE) )
    {
      sw.writeAttribute("from", mEvent.getSender().resourceAddress());
      sw.writeAttribute("type", "unavailable");
      sw.writeEndElement();
    }
    else
    {
      sw.writeAttribute("from", mEvent.getSender().resourceAddress());
      sw.writeStartElement("jabber:client", "show");
      sw.writeCharacters(getShow(status));
      sw.writeEndElement();
      if( !mEvent.getStatus().getText().isEmpty() )
      {
        sw.writeStartElement("jabber:client","status");
        sw.writeCharacters(mEvent.getStatus().getText());
        sw.writeEndElement();
      }
    }

    sw.close();
  }

  private String getShow(Status status)
  {
    switch (status.getType())
    {
      case BUSY:
        return "dnd";

      case AWAY:
        return "away";

      default:
      case AVAILABLE:
        return "chat";
    }
  }
}
