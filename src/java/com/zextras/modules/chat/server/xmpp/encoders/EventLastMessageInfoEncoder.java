/*
 * Copyright (C) 2018 ZeXtras S.r.l.
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
import com.zextras.modules.chat.server.events.EventLastMessageInfo;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

public class EventLastMessageInfoEncoder extends XmppEncoder
{
  private final EventLastMessageInfo mEvent;

  public EventLastMessageInfoEncoder(
    EventLastMessageInfo event,
    SchemaProvider schemaProvider
  )
  {
    super("", schemaProvider);
    mEvent = event;
  }

  @Override
  public void encode(OutputStream outputStream, SpecificAddress target, boolean extensions) throws XMLStreamException
  {
    XMLStreamWriter2 sw = getStreamWriter(outputStream);

    sw.writeStartElement("", "last_message_info");
    sw.writeAttribute("id", mEvent.getId().toString());
    if (mEvent.getLastSentMessageInfo().hasValue())
    {
      sw.writeAttribute("last_message_sent_timestamp", String.valueOf(mEvent.getLastSentMessageInfo().get().getLeft()));
      sw.writeAttribute("last_message_sent_id", mEvent.getLastSentMessageInfo().get().getRight());
    }
    if (mEvent.getLastIncomingMessageInfo().hasValue())
    {
      sw.writeAttribute("last_message_received_timestamp", String.valueOf(mEvent.getLastIncomingMessageInfo().get().getLeft()));
      sw.writeAttribute("last_message_received_id", mEvent.getLastIncomingMessageInfo().get().getRight());
    }
    if (mEvent.getUnreadCount().hasValue())
    {
      sw.writeAttribute("unread_count", String.valueOf(mEvent.getUnreadCount()));
    }
    if (mEvent.getFinalDestination().hasValue())
    {
      sw.writeAttribute("final_destination", mEvent.getFinalDestination().get().resourceAddress());
    }
    if (mEvent.getBuddyAddress().hasValue())
    {
      sw.writeAttribute("buddy_address", mEvent.getBuddyAddress().get().resourceAddress());
    }
    sw.writeAttribute("timestamp", String.valueOf(mEvent.getTimestamp()));
    sw.writeAttribute("target", mEvent.getTarget().toSingleAddressIncludeResource());
    sw.writeAttribute("sender", mEvent.getSender().resourceAddress());
    sw.writeEndElement();

    sw.close();
  }
}
