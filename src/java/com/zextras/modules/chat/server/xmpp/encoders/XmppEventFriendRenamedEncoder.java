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
import com.zextras.modules.chat.server.events.EventFriendRenamed;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

public class XmppEventFriendRenamedEncoder extends XmppEncoder
{
  private final EventFriendRenamed mEvent;

  protected XmppEventFriendRenamedEncoder(
    EventFriendRenamed eventFriendRenamed,
    SchemaProvider schemaProvider
  )
  {
    super("jabber-client.xsd", schemaProvider);
    mEvent = eventFriendRenamed;
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

<iq xmlns="jabber:client" type="result" id="purplefdc3ac6d" to="john@example.com/b51198d2-b27a-499a-ab19-2325d591a794">
<query xmlns="jabber:iq:roster" ver="ver7">
  <item jid="chat@example.com" name="chat test 12287" subscription="both">
  <group>zimbra</group>
  </item>
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

    sw.writeStartElement("jabber:iq:roster", "item");
    sw.writeAttribute("jid", mEvent.getFriendToRename().toString());
    sw.writeAttribute("name", mEvent.getNewNickname());
    sw.writeAttribute("subscription", "both");

    sw.writeStartElement("jabber:iq:roster","group");
    sw.writeCharacters(mEvent.getNewGroup());
    sw.writeEndElement();
    sw.writeEndElement();

    sw.writeEndElement();
    sw.writeEndElement();
    sw.close();
  }
}
