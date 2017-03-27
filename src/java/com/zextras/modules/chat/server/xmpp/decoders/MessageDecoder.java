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

package com.zextras.modules.chat.server.xmpp.decoders;

import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.WritingState;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.events.EventIsWriting;
import com.zextras.modules.chat.server.events.EventMessage;
import com.zextras.modules.chat.server.xmpp.parsers.MessageParser;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class MessageDecoder implements EventDecoder
{
  private final SchemaProvider mSchemaProvider;

  public MessageDecoder(SchemaProvider schemaProvider)
  {
    mSchemaProvider = schemaProvider;
  }

  @Override
  public List<Event> decode(InputStream inputStream) throws XMLStreamException
  {
    MessageParser parser = new MessageParser(inputStream, mSchemaProvider);
    parser.parse();

    String eventId = parser.getId();
    if (eventId.isEmpty()) {
      eventId = EventId.randomUUID().toString();
    }

    List<Event> eventList = new ArrayList(3);

    SpecificAddress targetAddress = new SpecificAddress(parser.getTo());
    SpecificAddress sender = new SpecificAddress(parser.getFrom());
    Target target = new Target( targetAddress );

    if( parser.isComposing() )
    {
      Event event = new EventIsWriting(
        sender,
        target,
        WritingState.WRITING
      );
      eventList.add(event);
    }

    if( parser.isGone()  )
    {
      Event event = new EventIsWriting(
        sender,
        target,
        WritingState.GONE
      );
      eventList.add(event);
    }

    if( parser.isActive()  )
    {
      Event event = new EventIsWriting(
        sender,
        target,
        WritingState.STOPPED
      );
      eventList.add(event);
    }

    if( parser.isInactive()  )
    {
      Event event = new EventIsWriting(
        sender,
        target,
        WritingState.RESET
      );
      eventList.add(event);
    }

    if( !parser.getBody().isEmpty() )
    {
      Event message = new EventMessage(
        EventId.fromString(eventId),
        sender,
        target,
        parser.getBody()
      );
      eventList.add(message);
    }

    return eventList;
  }
}
