package com.zextras.modules.chat.server.xmpp.decoders;

import com.google.common.base.Optional;
import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.events.EventMessageHistoryLast;
import com.zextras.modules.chat.server.xmpp.parsers.MessageHistoryLastParser;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.openzal.zal.Utils;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class MessageHistoryLastDecoder implements EventDecoder
{
  private final SchemaProvider mSchemaProvider;

  public MessageHistoryLastDecoder(SchemaProvider schemaProvider)
  {
    mSchemaProvider = schemaProvider;
  }

  @Override
  public List<Event> decode(InputStream inputStream) throws XMLStreamException
  {
    MessageHistoryLastParser parser = new MessageHistoryLastParser(inputStream, mSchemaProvider);
    parser.parse();

    String eventId = parser.getId();
    if (eventId.isEmpty())
    {
      eventId = EventId.randomUUID().toString();
    }
    long timestamp = System.currentTimeMillis();

    try
    {
      String s = parser.getTimestamp();
      if (!s.isEmpty())
      {
        timestamp = Long.getLong(s);
      }
    }
    catch (RuntimeException e)
    {
      ChatLog.log.warn(Utils.exceptionToString(e));
    }

    Optional<Integer> count = Optional.<Integer>absent();
    try
    {
      String s = parser.getCount();
      if (!s.isEmpty())
      {
        count = Optional.<Integer>of(Integer.valueOf(s));
      }
    }
    catch (RuntimeException e)
    {
      ChatLog.log.warn(Utils.exceptionToString(e));
    }
    return Collections.<Event>singletonList(new EventMessageHistoryLast(
      EventId.fromString(eventId),
      new SpecificAddress(parser.getSender()),
      parser.getQueryId(),
      new SpecificAddress(parser.getTo()),
      parser.getFirst(),
      parser.getLast(),
      count,
      timestamp
    ));
  }
}
