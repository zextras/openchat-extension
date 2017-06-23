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

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

public class BindParser extends XmppParser {
  private String mBindId = "";
  private String mType = "";
  private String mResource = "";

  public BindParser(
    InputStream xmlInput,
    SchemaProvider schemaProvider)
  {
    super("jabber-client.xsd", xmlInput, schemaProvider);
  }

  // <iq type='set' id='purplee75f1d8e'>
  //   <bind xmlns='urn:ietf:params:xml:ns:xmpp-bind'>
  //     <resource>marco</resource>
  //   </bind>
  // </iq>

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
        case XMLStreamReader.START_ELEMENT: {
          mBindId = sr.getAttributeValue(null, "id");
          mType = sr.getAttributeValue(null, "type");
          parseBind(sr);
        }
      }
    }
  }

  private void parseBind(XMLStreamReader2 sr) throws XMLStreamException {
    String last = "";
    while (sr.hasNext()) {
      switch (sr.getEventType()) {
        case XMLStreamReader.START_ELEMENT:
        {
          last = sr.getLocalName();
          break;
        }

        case XMLStreamConstants.CHARACTERS:
        {
          if (last.equals("resource")) {
            mResource = sr.getText();
          }
          break;
        }

        case XMLStreamReader.END_ELEMENT:
        {
          last = "";
          break;
        }
      }

      sr.next();
    }
  }

  public String getBindId() {
    return mBindId;
  }

  public String getType() {
    return mType;
  }

  public String getResource() {
    return mResource;
  }
}
