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

import com.zextras.modules.chat.server.xmpp.ConnectionType;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamReader2;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

public class StreamXmppParser extends XmppParser
{
  private String         mDomain  = "";
  private ConnectionType mType    = ConnectionType.Client;
  private boolean        mIsProxy = false;


  public StreamXmppParser(InputStream inputStream, SchemaProvider schemaProvider)
  {
    super("streams.xsd", inputStream, schemaProvider);
  }

  @Override
  public void parse() throws XMLStreamException
  {
    XMLStreamReader2 sr = getStreamReader();

    while (sr.hasNext())
    {
      switch (sr.getEventType())
      {
        case XMLStreamReader.START_ELEMENT: {
          mDomain = sr.getAttributeValue(null,"to");
          String isProxy = sr.getAttributeValue("http://zextras.com/xmpp", "proxy");
          mIsProxy = isProxy != null && isProxy.equalsIgnoreCase("true");
          String type = sr.getNamespaceContext().getNamespaceURI("");
          mType = ConnectionType.fromXmpp(type);
          return;
        }
      }

      sr.next();
    }
  }

  public ConnectionType getType()
  {
    return mType;
  }

  public String getDomain()
  {
    return emptyStringWhenNull(mDomain);
  }

  public boolean isProxy()
  {
    return mIsProxy;
  }
}
