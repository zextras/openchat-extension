/*
 * ZAL - The abstraction layer for Zimbra.
 * Copyright (C) 2017 ZeXtras S.r.l.
 *
 * This file is part of ZAL.
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
 * You should have received a copy of the GNU General Public License
 * along with ZAL. If not, see <http://www.gnu.org/licenses/>.
 */

package com.zextras.modules.chat.server.xmpp.parsers;

import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamReader2;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;


public class DiscoveryParser extends XmppParser
{

  private String mType;
  private String mIQId;
  private String mTarget;

  public DiscoveryParser(InputStream xmlInput, SchemaProvider schemaProvider) {
    super("disco-info.xsd", xmlInput, schemaProvider);
  }

  // <iq type='get' id='purple841b5737' to='example.com'><query xmlns='http://jabber.org/protocol/disco#items'/></iq>

  @Override
  public void parse() throws XMLStreamException {
    XMLStreamReader2 sr = getStreamReader();

    if (validate()) {
      sr.validateAgainst(getDefaultSchema());
    }

    while (sr.hasNext()) {
      sr.next();
      switch (sr.getEventType()) {
        case XMLStreamReader2.START_ELEMENT: {
          if (sr.getLocalName().equals("iq")) {
            mType = sr.getAttributeValue(null, "type");
            mIQId = sr.getAttributeValue(null, "id");
            mTarget = sr.getAttributeValue(null, "to");
          }
        }
      }
    }
  }

  public String getType() {
    return mType;
  }

  public String getIQId() {
    return mIQId;
  }

  public String getTo() {
    return mTarget;
  }
}
