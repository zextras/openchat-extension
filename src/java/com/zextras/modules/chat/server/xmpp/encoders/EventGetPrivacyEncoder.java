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
import com.zextras.modules.chat.server.events.EventGetPrivacy;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

public class EventGetPrivacyEncoder extends XmppEncoder
{
  private final EventGetPrivacy mEvent;

  public EventGetPrivacyEncoder(EventGetPrivacy eventGetPrivacy, SchemaProvider schemaProvider)
  {
    super("streams.xsd", schemaProvider);
    mEvent = eventGetPrivacy;
  }

/*
<iq type='result' id='getlist1' to='romeo@example.net/orchard'>
  <query xmlns='jabber:iq:privacy'>
    <active name='private'/>
    <default name='public'/>
    <list name='public'/>
    <list name='private'/>
    <list name='special'/>
  </query>
</iq>
 */

  @Override
  public void encode(OutputStream outputStream, SpecificAddress target) throws XMLStreamException
  {
    XMLStreamWriter2 sr = getStreamWriter(outputStream);
    if( validate() ) {
      sr.validateAgainst(getDefaultSchema());
    }

    sr.setPrefix("","jabber:client");
    sr.writeStartElement("","iq","jabber:client");
    sr.writeAttribute("type","result");
    sr.writeAttribute("id",mEvent.getId().toString());
    sr.writeAttribute("to",target.resourceAddress());

    sr.writeStartElement("","query","jabber:iq:privacy");
    sr.writeEndElement();
    sr.writeEndElement();

    sr.close();
  }
}
