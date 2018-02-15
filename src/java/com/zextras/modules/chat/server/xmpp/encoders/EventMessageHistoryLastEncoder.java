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
import com.zextras.modules.chat.server.events.EventMessage;
import com.zextras.modules.chat.server.events.EventMessageHistory;
import com.zextras.modules.chat.server.events.EventMessageHistoryLast;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @see EventMessageHistoryLast
 * @see EventMessageHistoryLastEncoder
 * @see com.zextras.modules.chat.server.xmpp.parsers.MessageHistoryLastParser
 */
public class EventMessageHistoryLastEncoder extends XmppEncoder
{
  private final EventMessageHistoryLast mEvent;

/*
<iq type='result' id='juliet1'>
  <fin xmlns='urn:xmpp:mam:2'>
    <set xmlns='http://jabber.org/protocol/rsm'>
      <first index='0'>28482-98726-73623</first>
      <last>09af3-cc343-b409f</last>
    </set>
  </fin>
</iq>
*/

  public EventMessageHistoryLastEncoder(EventMessageHistoryLast event, SchemaProvider schemaProvider)
  {
    super("jabber-client.xsd", schemaProvider);
    mEvent = event;
  }

  @Override
  public void encode(OutputStream outputStream, SpecificAddress target, boolean extensions) throws XMLStreamException
  {
    XMLStreamWriter2 sw = getStreamWriter(outputStream);
    if( validate() ) {
      sw.validateAgainst(getDefaultSchema());
    }

    sw.writeStartElement("", "iq");
      sw.writeAttribute("type", "result");
      sw.writeAttribute("id", mEvent.getId().toString());
      sw.writeAttribute("from", mEvent.getSender().toString()); // not really standard
      sw.writeAttribute("to", target.resourceAddress()); // not really standard

      sw.writeStartElement("","fin","urn:xmpp:mam:2" );
        if (!StringUtils.isEmpty(mEvent.getQueryId()))
        {
          sw.writeAttribute("queryid", mEvent.getQueryId());
        }

        sw.writeStartElement("","set","http://jabber.org/protocol/rsm" );
          if (!StringUtils.isEmpty(mEvent.getFirstId()))
          {
            sw.writeStartElement("first");
            sw.writeAttribute("index", "0");
            sw.writeCharacters(mEvent.getFirstId());
            sw.writeEndElement();
          }
          if (!StringUtils.isEmpty(mEvent.getLastId()))
          {
            sw.writeStartElement("last");
            sw.writeCharacters(mEvent.getLastId());
            sw.writeEndElement();
          }
        sw.writeEndElement();
      sw.writeEndElement();
    sw.writeEndElement();
    sw.close();
  }
}
