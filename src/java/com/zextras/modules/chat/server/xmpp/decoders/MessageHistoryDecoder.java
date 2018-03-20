package com.zextras.modules.chat.server.xmpp.decoders;

import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.events.EventMessageHistory;
import com.zextras.modules.chat.server.xmpp.parsers.MessageHistoryParser;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class MessageHistoryDecoder implements EventDecoder
{
  private final SchemaProvider mSchemaProvider;

  public MessageHistoryDecoder(SchemaProvider schemaProvider)
  {
    mSchemaProvider = schemaProvider;
  }

  @Override
  public List<Event> decode(InputStream inputStream) throws XMLStreamException
  {
    MessageHistoryParser parser = new MessageHistoryParser(inputStream, mSchemaProvider);
    parser.parse();

    String eventId = parser.getId();
    if (eventId.isEmpty())
    {
      eventId = EventId.randomUUID().toString();
    }
    return Collections.<Event>singletonList(new EventMessageHistory(
      EventId.fromString(eventId),
      new SpecificAddress(parser.getSender()),
      parser.getQueryId(),
      new SpecificAddress(parser.getTo()),
      parser.getEvent(),
      parser.getTimestamp()
    ));
  }
}
