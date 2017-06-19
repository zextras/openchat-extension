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
import com.zextras.modules.chat.server.events.FeatureNotImplementedEvent;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

public class FeatureNotImplementedEncoder extends XmppEncoder
{
  private final FeatureNotImplementedEvent mFeatureNotImplementedEvent;

  public FeatureNotImplementedEncoder(
    FeatureNotImplementedEvent featureNotImplementedEvent,
    SchemaProvider schemaProvider
  )
  {
    super("jabber-client.xsd", schemaProvider);
    mFeatureNotImplementedEvent = featureNotImplementedEvent;
  }

  @Override
  public void encode(OutputStream outputStream, SpecificAddress target) throws XMLStreamException
  {
    XMLStreamWriter2 sw = getStreamWriter(outputStream);

    if (validate())
    {
      sw.validateAgainst(getDefaultSchema());
    }

    sw.writeStartElement("","iq","jabber:client");
    sw.writeAttribute("type","error");
    sw.writeAttribute("id", mFeatureNotImplementedEvent.getRequestId());
    if (!mFeatureNotImplementedEvent.getOriginalReceiver().isEmpty())
    {
      sw.writeAttribute("from", mFeatureNotImplementedEvent.getOriginalReceiver());
    }
    sw.writeAttribute("to", mFeatureNotImplementedEvent.getOriginalSender().toString());
    sw.writeEmptyElement("","feature-not-implemented", "urn:ietf:params:xml:ns:xmpp-stanzas");
    sw.writeEndElement();

    sw.close();
  }
}
