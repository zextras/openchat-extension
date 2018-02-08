package com.zextras.modules.chat.server.xmpp.decoders;

import com.zextras.lib.DateUtils;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.events.EventMessage;
import com.zextras.modules.chat.server.events.EventMessageHistory;
import com.zextras.modules.chat.server.xmpp.decoders.EventDecoder;
import com.zextras.modules.chat.server.xmpp.parsers.IQQueryXmppParser;
import com.zextras.modules.chat.server.xmpp.parsers.MessageHistoryParser;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.openzal.zal.lib.FakeClock;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
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
    try
    {
      return Collections.<Event>singletonList(new EventMessageHistory(
        EventId.fromString(eventId),
        parser.getQueryId(),
        new SpecificAddress(parser.getTo()),
        new EventMessage(
          EventId.fromString(parser.getMessageId()),
          new SpecificAddress(parser.getMessageFrom()),
          new Target(new SpecificAddress(parser.getMessageTo())),
          parser.getBody(),
          new FakeClock(DateUtils.parseUTCDate(parser.getMessageStamp()))
        )
      ));
    } catch (ParseException e)
    {
      throw new XMLStreamException(e.getMessage());
    }
  }
}
