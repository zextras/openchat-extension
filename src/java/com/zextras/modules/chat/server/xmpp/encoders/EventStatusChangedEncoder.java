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

import com.zextras.modules.chat.server.events.EventType;
import com.zextras.modules.chat.server.status.Status;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.EventStatusChanged;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

public class EventStatusChangedEncoder extends XmppEncoder
{
  public static final String sProtocolZextrasStatus = "http://zextras.com/protocol/chat/status";

  private final EventStatusChanged mEvent;

  protected EventStatusChangedEncoder(EventStatusChanged event, SchemaProvider schemaProvider)
  {
    super("jabber-client.xsd", schemaProvider);
    mEvent = event;
  }

  @Override
  public void encode(OutputStream outputStream, SpecificAddress target, boolean extensions) throws XMLStreamException
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

<presence from='romeo@example.net/orchard' xml:lang='en'>
  <show>unsubscribed</show>
  <status>Wooing Juliet</status>
  <status xml:lang='cs'>Dvo&#x0159;&#x00ED;m se Julii</status>
  <x ns="http://zextras.com/chat/status" validSince="123">
    <meeting jid="meeting1@example.com" />
    <meeting jid="meeting1@example.com" />
  </x>
</presence>

online: chat
away: away
busy: dnd
*/
    sw.writeStartElement("", "presence", "jabber:client");
    // sw.writeAttribute("id", mEvent.getId().toString());
    sw.writeAttribute("to", target.resourceAddress());

    Status status = mEvent.getStatus();
    if(   status.getType().equals(Status.StatusType.INVISIBLE)
       || status.getType().equals(Status.StatusType.OFFLINE) )
    {
      sw.writeAttribute("from", mEvent.getSender().resourceAddress());
      sw.writeAttribute("type", "unavailable");
    }
    else
    {
      sw.writeAttribute("from", mEvent.getSender().resourceAddress());
      sw.writeStartElement("jabber:client", "show");
      sw.writeCharacters(getShow(status).toLowerCase());
      sw.writeEndElement();
      if( !mEvent.getStatus().getText().isEmpty() )
      {
        sw.writeStartElement("jabber:client","status");
        sw.writeCharacters(mEvent.getStatus().getText());
        sw.writeEndElement();
      }
    }

    if( extensions )
    {
      sw.writeStartElement("", "x", sProtocolZextrasStatus);
      sw.writeAttribute("validSince", String.valueOf(mEvent.getStatus().validSince()));
      sw.writeAttribute("groupType", String.valueOf(mEvent.getType()).toLowerCase());
      for (SpecificAddress meeting : mEvent.getStatus().meetings())
      {
        sw.writeStartElement("meeting");
        sw.writeAttribute("jid", meeting.toString());
        sw.writeEndElement();
      }
      sw.writeEndElement();
    }
    else
    {
      if(mEvent.getType() != EventType.Chat)
      {
        sw.writeStartElement("", "x", "http://jabber.org/protocol/muc#user");
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

      case AVAILABLE:
        return "chat";

      default:
        return status.getType().name();
    }
  }
}
