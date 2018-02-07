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
import com.zextras.modules.chat.server.events.EventIsWriting;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

public class EventIsWritingEncoder extends XmppEncoder
{
  private final EventIsWriting mEvent;

  protected EventIsWritingEncoder(EventIsWriting event, SchemaProvider schemaProvider)
  {
    super("jabber-client.xsd", schemaProvider);
    mEvent = event;
  }

  @Override
  public void encode(OutputStream outputStream, SpecificAddress target, boolean extensions) throws XMLStreamException
  {
    XMLStreamWriter2 sw = getStreamWriter(outputStream);

    if( validate() ) {
      sw.validateAgainst(getDefaultSchema());
    }

    sw.writeStartElement("","message","jabber:client");
    sw.writeAttribute("type", "chat");
    sw.writeAttribute("from", mEvent.getSender().resourceAddress());
    sw.writeAttribute("to", target.resourceAddress());
    sw.writeAttribute("id",mEvent.getId().toString());
    encodeWritingType(sw);
    sw.writeEndElement();

    sw.close();
  }

  private void encodeWritingType(XMLStreamWriter2 sw) throws XMLStreamException
  {
    if( validate() ) {
      sw.validateAgainst(getSchema("chatstates.xsd"));
    }

    switch( mEvent.getState() )
    {
      case WRITING:
        sw.writeEmptyElement("", "composing","http://jabber.org/protocol/chatstates");
        break;

      case STOPPED:
        sw.writeEmptyElement("", "active","http://jabber.org/protocol/chatstates");
        break;

      case GONE:
        sw.writeEmptyElement("", "gone","http://jabber.org/protocol/chatstates");
        break;

      case RESET:
        sw.writeEmptyElement("", "inactive","http://jabber.org/protocol/chatstates");
        break;
    }
  }
}
