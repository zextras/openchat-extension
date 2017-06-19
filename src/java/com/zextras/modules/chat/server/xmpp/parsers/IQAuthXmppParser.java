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

public class IQAuthXmppParser extends XmppParser
{
  private String mDomain   = "";
  private String mUsername = "";
  private String mPassword = "";
  private String mRequestId = "";
  private String mResource = "";

  public IQAuthXmppParser(
    InputStream xmlInput,
    SchemaProvider schemaProvider
  )
  {
    super("jabber-client.xsd", xmlInput, schemaProvider);
  }

/*
<iq type="get" to="example.com" id="auth_1">
  <query xmlns="jabber:iq:auth">
    <username>davide</username>
  </query>
</iq>
*/

  @Override
  public void parse() throws XMLStreamException
  {
    XMLStreamReader2 sr = getStreamReader();

    if( validate() ){
      sr.validateAgainst(getDefaultSchema());
    }

    while (sr.hasNext())
    {
      sr.next();

      switch (sr.getEventType())
      {
        case XMLStreamReader.START_ELEMENT: {
          mDomain = emptyStringWhenNull(sr.getAttributeValue(null,"to"));
          mRequestId = emptyStringWhenNull(sr.getAttributeValue(null,"id"));
          parseQuery(sr);
          break;
        }
      }
    }
  }

  private void parseQuery(XMLStreamReader2 sr)
    throws XMLStreamException
  {
    if( validate() ){
      sr.validateAgainst(getSchema("iq-auth.xsd"));
    }

    String last = "";

    while(sr.hasNext())
    {
      sr.next();

      switch (sr.getEventType())
      {
        case XMLStreamReader.START_ELEMENT:
        {
          last = sr.getLocalName();
          break;
        }

        case XMLStreamReader.END_ELEMENT:
        {
          last = "";
          break;
        }

        case XMLStreamReader.CHARACTERS:
        {
          if( last.equals("username")) {
            mUsername = emptyStringWhenNull(sr.getText());
          }
          if(last.equals("password")) {
            mPassword = emptyStringWhenNull(sr.getText());
          }
          if(last.equals("resource")) {
            mResource = emptyStringWhenNull(sr.getText());
          }
          break;
        }
      }
    }

    sr.validateAgainst(getDefaultSchema());
  }

  public String getDomain()
  {
    return mDomain;
  }

  public String getUsername()
  {
    return mUsername;
  }

  public String getEventId()
  {
    return mRequestId;
  }

  public String getPassword()
  {
    return mPassword;
  }

  public String getResource()
  {
    return mResource;
  }
}
