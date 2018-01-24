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
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @see EventMessageHistory
 * @see EventMessageHistoryEncoder
 * @see com.zextras.modules.chat.server.xmpp.parsers.MessageHistoryParser
 */
public class EventMessageHistoryEncoder extends XmppEncoder
{
  private static final String CURRENT_XMPP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  private final EventMessageHistory mEvent;

/*
<message id='aeb213' to='juliet@capulet.lit/chamber'>
  <result xmlns='urn:xmpp:mam:2' queryid='f27' id='28482-98726-73623'>
  <forwarded xmlns='urn:xmpp:forward:0'>
  <delay xmlns='urn:xmpp:delay' stamp='2010-07-10T23:08:25Z'/>
  <message xmlns='jabber:client' from="witch@shakespeare.lit" to="macbeth@shakespeare.lit">
  <body>Hail to thee</body>
  </message>
  </forwarded>
  </result>
  </message>
*/


  public EventMessageHistoryEncoder(EventMessageHistory event, SchemaProvider schemaProvider)
  {
    super("jabber-client.xsd", schemaProvider);
    mEvent = event;
  }

  @Override
  public void encode(OutputStream outputStream, SpecificAddress target) throws XMLStreamException
  {
    XMLStreamWriter2 sw = getStreamWriter(outputStream);
    if( validate() ) {
      sw.validateAgainst(getDefaultSchema());
    }

    EventMessage message = mEvent.getOriginalMessage();

    sw.writeStartElement("", "message");
      sw.writeAttribute("id", mEvent.getId().toString());
      sw.writeAttribute("to", target.resourceAddress());

      sw.writeStartElement("","result","urn:xmpp:mam:2" );
        sw.writeAttribute("queryid",mEvent.getQueryId());
        sw.writeAttribute("id",message.getId().toString());

        sw.setPrefix("","urn:xmpp:forward:0");
        sw.writeStartElement("urn:xmpp:forward:0", "forwarded");

          sw.writeStartElement("","delay","urn:xmpp:delay" );
          sw.writeAttribute("stamp",convertLongToUTCDateString(message.getTimestamp(), CURRENT_XMPP_FORMAT));
          sw.writeEndElement();

          sw.writeStartElement("","message","jabber:client");
            sw.writeAttribute("from", message.getSender().toString());
            sw.writeAttribute("to", message.getTarget().toString());

            sw.writeStartElement("jabber:client", "body");
              sw.writeCharacters(message.getMessage());
            sw.writeEndElement();
          sw.writeEndElement();
        sw.writeEndElement();
      sw.writeEndElement();
    sw.writeEndElement();
    sw.close();
  }

  private String convertLongToUTCDateString(long timestamp, String format)
  {
    Date messageDate = new Date(timestamp);
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    return sdf.format(messageDate);
  }
}
