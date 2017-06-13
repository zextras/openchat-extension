package com.zextras.modules.chat.server.xmpp.encoders;

import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.EventFloodControl;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

public class EventFloodControlEncoder extends XmppEncoder
{
  private final EventFloodControl mEvent;

  public EventFloodControlEncoder(EventFloodControl event, SchemaProvider schemaProvider)
  {
    super("jabber-client.xsd", schemaProvider);
    mEvent = event;
  }

  @Override
  public void encode(OutputStream outputStream, SpecificAddress target) throws XMLStreamException
  {
    XMLStreamWriter2 sw = getStreamWriter(outputStream);

    sw.writeStartElement("", "floodcontrol");
    sw.writeAttribute("flood_detected", String.valueOf(mEvent.isFloodDetected()));
    sw.writeAttribute("address", target.resourceAddress());
    sw.writeEndElement();

    sw.close();
  }
}
