package com.zextras.modules.chat.server.xmpp.decoders;

import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventIQQuery;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.xmpp.parsers.IQQueryXmppParser;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class QueryDecoder implements EventDecoder
{
  private final SchemaProvider mSchemaProvider;

  public QueryDecoder(SchemaProvider schemaProvider)
  {
    mSchemaProvider = schemaProvider;
  }

  @Override
  public List<Event> decode(InputStream inputStream) throws XMLStreamException
  {
    IQQueryXmppParser parser = new IQQueryXmppParser(inputStream, mSchemaProvider);
    parser.parse();

    String eventId = parser.getId();
    if (eventId.isEmpty())
    {
      eventId = EventId.randomUUID().toString();
    }

    return Collections.<Event>singletonList(new EventIQQuery(
      EventId.fromString(eventId),
      parser.getSender(),
      parser.getQueryId(),
      new Target(new SpecificAddress(parser.getTo())),
      parser.getNode(),
      parser.getWith(),
      parser.getStart(),
      parser.getEnd(),
      parser.getMax()
    ));
  }
}
