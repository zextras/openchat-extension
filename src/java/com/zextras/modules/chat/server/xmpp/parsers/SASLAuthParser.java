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


public class SASLAuthParser extends XmppParser
{
  private String mMechanism;
  private String mAuthString;

  public SASLAuthParser(
    InputStream xmlInputStream,
    SchemaProvider schemaProvider
  )
  {
    super("jabber-client.xsd", xmlInputStream, schemaProvider);
  }

// <auth xmlns="urn:ietf:params:xml:ns:xmpp-sasl" mechanism="SCRAM-SHA-1">
//   biwsbj1qdWxpZXQscj1vTXNUQUF3QUFBQU1BQUFBTlAwVEFBQUFBQUJQVTBBQQ==
// </auth>

  @Override
  public void parse() throws XMLStreamException
  {
    XMLStreamReader2 sr = getStreamReader();

    if (validate()) {
      sr.validateAgainst(getDefaultSchema());
    }

    while (sr.hasNext())
    {
      sr.next();

      switch (sr.getEventType())
      {
        case XMLStreamReader.START_ELEMENT: {
          mMechanism = sr.getAttributeValue(null, "mechanism");
          mAuthString = sr.getElementText();
        }
      }
    }
  }

  public String getMechanism() {
    return mMechanism;
  }

  public String getAuthString() {
    return mAuthString;
  }
}
