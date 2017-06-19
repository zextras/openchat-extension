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

import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamReader2;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

public class EventStatusProbeParser extends XmppParser
{
  private String mFrom = "";
  private String mTo = "";

  public EventStatusProbeParser(InputStream xmlInputStream, SchemaProvider schemaProvider)
  {
    super("jabber-client.xsd", xmlInputStream, schemaProvider);
  }

  @Override
  public void parse() throws XMLStreamException
  {
    XMLStreamReader2 sr = getStreamReader();

    if (validate()) {
      sr.validateAgainst(getDefaultSchema());
    }

    while( sr.hasNext() )
    {
      sr.next();

      switch ( sr.getEventType() )
      {
        case XMLStreamReader.START_ELEMENT:
        {
          mTo = emptyStringWhenNull(sr.getAttributeValue(null,"to"));
          mFrom = emptyStringWhenNull(sr.getAttributeValue(null,"from"));
          return;
        }
      }
    }
  }

  public String getFrom()
  {
    return mFrom;
  }

  public String getTo()
  {
    return mTo;
  }
}
