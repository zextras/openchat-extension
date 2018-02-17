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
import com.zextras.modules.chat.server.events.EventMessageAck;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class EventMessageAckEncoder extends XmppEncoder
{
  private static final String CURRENT_XMPP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  private final EventMessageAck mEventMessageAck;

  public EventMessageAckEncoder(EventMessageAck eventMessageAck, SchemaProvider schemaProvider)
  {
    super("jabber-client.xsd", schemaProvider);
    mEventMessageAck = eventMessageAck;
  }

/*
<message
    from='kingrichard@royalty.england.lit/throne'
    id='bi29sg183b4v'
    to='northumberland@shakespeare.lit/westminster'>
  <received xmlns='urn:xmpp:receipts' id='richard2-4.1.247'/>
</message>
 */
  @Override
  public void encode(OutputStream outputStream, SpecificAddress target, boolean extensions) throws XMLStreamException
  {
    XMLStreamWriter2 sw = getStreamWriter(outputStream);

    if( validate() ) {
      sw.validateAgainst(getDefaultSchema());
    }

    sw.writeStartElement("","message","jabber:client");
    sw.writeAttribute("from", mEventMessageAck.getSender().resourceAddress());
    sw.writeAttribute("to", target.resourceAddress());
    sw.writeAttribute("id",mEventMessageAck.getId().toString());
    sw.writeAttribute("timestamp",convertLongToUTCDateString(mEventMessageAck.getTimestamp(), CURRENT_XMPP_FORMAT));

    if( validate() ) {
      sw.validateAgainst(getSchema("receipts.xsd"));
    }
    sw.writeEmptyElement("","received","urn:xmpp:receipts");
    sw.writeAttribute("id",mEventMessageAck.getMessageId().toString());

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
