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
import com.zextras.modules.chat.server.events.EventDiscovery;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;


public class DiscoveryEncoder extends XmppEncoder
{
  private final EventDiscovery mEvent;

  public DiscoveryEncoder(
    EventDiscovery event,
    SchemaProvider schemaProvider
  )
  {
    super("jabber-client.xsd", schemaProvider);
    mEvent = event;
  }
/*
  <iq type='result'
    from='shakespeare.lit'
    to='romeo@montague.net/orchard'
    id='items1'>
    <query xmlns='http://jabber.org/protocol/disco#items'/>
  </iq>

  <iq type="error" id="purplea0645785" from="studiostorti.com" to="davide@studiostorti.com/pidgin">
    <query xmlns="http://jabber.org/protocol/disco#info"/>
    <error type='cancel'>
      <service-unavailable xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>
    </error>
  </iq>
 */

  @Override
  public void encode(OutputStream outputStream, SpecificAddress target, boolean extensions) throws XMLStreamException
  {
    XMLStreamWriter2 sw = getStreamWriter(outputStream);

    if( validate() )
    {
      sw.validateAgainst(getDefaultSchema());
    }

    sw.writeStartElement("","iq","jabber:client");
    sw.writeAttribute("type", mEvent.getType());

    sw.writeAttribute("id", mEvent.getId().toString());

    sw.writeAttribute("from", mEvent.getSender().resourceAddress());

    sw.writeAttribute("to", target.resourceAddress());

    if( validate() )
    {
      switch (mEvent.getDiscoveryQuery())
      {
        case info:
          sw.validateAgainst(getSchema("disco-info.xsd"));
          break;
        case items:
          sw.validateAgainst(getSchema("disco-items.xsd"));
          break;
      }
    }

    sw.writeStartElement("", "query", "jabber:client");
    sw.writeAttribute("xmlns", mEvent.getDiscoveryQuery().getUrl());

    for (String feature : mEvent.getFeatures())
    {
      sw.writeStartElement("", "feature", "jabber:client");
      sw.writeAttribute("var", feature);
      sw.writeEndElement();
    }

    /*
    if( !mEvent.isDomainQuery() )
//    {
//      sw.writeEmptyElement("", "identity", "http://jabber.org/protocol/disco#info");
//      sw.writeAttribute("category","server");
//      sw.writeAttribute("type","xmpp");
//
//      sw.writeStartElement("", "feature", "http://jabber.org/protocol/disco#info");
//      sw.writeAttribute("var", "msgoffline");
//      sw.writeEndElement();
//      sw.writeEndElement();
//    }
//    else
    {
      sw.writeEndElement();

      sw.writeStartElement("","error","jabber:client");
      sw.writeAttribute("type","cancel");
      sw.writeEmptyElement("","service-unavailable", "urn:ietf:params:xml:ns:xmpp-stanzas");
      sw.writeEndElement();
    }
    */

    sw.writeEndElement();
    sw.close();
  }
}
