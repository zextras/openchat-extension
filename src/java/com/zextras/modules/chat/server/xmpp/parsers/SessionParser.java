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

package com.zextras.modules.chat.server.xmpp.parsers;

import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamReader2;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

public class SessionParser extends XmppParser
{
  private String mEventId = "";
  private String mType = "";
  private String mDomain = "";

  public SessionParser(
    InputStream xmlInput,
    SchemaProvider schemaProvider
  )
  {
    super("jabber-client.xsd", xmlInput, schemaProvider);
  }

//  <iq to='example.com' type='set' id='sess_1'>
//    <session xmlns='urn:ietf:params:xml:ns:xmpp-session'/>
//  </iq>

  @Override
  public void parse() throws XMLStreamException {
    XMLStreamReader2 sr = getStreamReader();

    if (validate()) {
      sr.validateAgainst(getDefaultSchema());
    }

    while (sr.hasNext())
    {
      sr.next();
      switch (sr.getEventType()) {
        case XMLStreamReader.START_ELEMENT:
        {
          if (sr.getLocalName().equals("iq")) {
            mEventId = emptyStringWhenNull(sr.getAttributeValue(null, "id"));
            mType = emptyStringWhenNull(sr.getAttributeValue(null, "type"));
            mDomain = emptyStringWhenNull(sr.getAttributeValue(null, "to"));
          }
          break;
        }
      }
    }
  }

  public EventId getEventId() {
    return new EventId(mEventId);
  }

  public String getType() {
    return mType;
  }

  public String getDomain()
  {
    return mDomain;
  }
}
