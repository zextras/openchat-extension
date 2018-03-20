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

package com.zextras.modules.chat.server.xmpp.decoders;

import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventLastMessageInfo;
import com.zextras.modules.chat.server.xmpp.parsers.LastMessageInfoParser;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class LastMessageInfoDecoder implements EventDecoder
{
  private final SchemaProvider mSchemaProvider;

  public LastMessageInfoDecoder(SchemaProvider schemaProvider)
  {
    mSchemaProvider = schemaProvider;
  }

  @Override
  public List<Event> decode(InputStream inputStream) throws XMLStreamException
  {
    LastMessageInfoParser parser = new LastMessageInfoParser(inputStream, mSchemaProvider);
    parser.parse();
    return Collections.<Event>singletonList(
      new EventLastMessageInfo(
        parser.getEventId(),
        parser.getSender(),
        new Target(parser.getTarget()),
        parser.getTimestamp(),
        parser.getBuddyAddress(),
        parser.getLastMessageSentInfo(),
        parser.getLastMessageReceivedInfo(),
        parser.getUnreadCount(),
        parser.getFinalDestination()
      )
    );
  }
}
