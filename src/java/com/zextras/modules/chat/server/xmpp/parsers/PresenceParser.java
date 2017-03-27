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

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

public class PresenceParser extends XmppParser
{
  public String getTo()
  {
    return mTo;
  }

  private String mTo = "";

  public String getType()
  {
    return mType;
  }

  private String mType = "";

  public String getShow()
  {
    return mShow;
  }

  public String getStatusText()
  {
    return mStatus;
  }

  public int getPriority()
  {
    return mPriority;
  }

  private String mShow     = "chat";
  private String mFrom     = "";
  private String mStatus   = "";
  private int    mPriority = 0;

  public PresenceParser(InputStream xmlInput, SchemaProvider schemaProvider)
  {
    super("jabber-client.xsd", xmlInput, schemaProvider);
  }

  /*
  <presence>
    <show>away</show>
    <status>I am gone right now, but I will be back later</status>
    <priority>5</priority>
    <c xmlns="http://jabber.org/protocol/caps" node="http://kopete.kde.org/jabber/caps" ver="1.2.5"/>
  </presence>

  <presence type="subscribed" to="admin@example.com"/>
  */
  @Override
  public void parse() throws XMLStreamException
  {
    XMLStreamReader2 sr = getStreamReader();

    while( sr.hasNext() )
    {
      sr.next();
      switch ( sr.getEventType() )
      {
        case XMLStreamReader.START_ELEMENT:
        {
          mFrom = emptyStringWhenNull(sr.getAttributeValue(null,"from")).toLowerCase();
          mTo = emptyStringWhenNull(sr.getAttributeValue(null,"to")).toLowerCase();
          mType = emptyStringWhenNull(sr.getAttributeValue(null,"type")).toLowerCase();
          parsePresenceTag(sr);
          break;
        }
      }
    }
  }

  private void parsePresenceTag(XMLStreamReader2 sr) throws XMLStreamException
  {
    String last = "";
    while( sr.hasNext() )
    {
      switch ( sr.getEventType() )
      {
        case XMLStreamReader.START_ELEMENT:
        {
          last = sr.getLocalName();
          break;
        }

        case XMLStreamReader.CHARACTERS:
        {
          if( last.equals("show")) {
            mShow = sr.getText().toLowerCase();
          }
          if(last.equals("status")) {
            mStatus = sr.getText();
          }
          if(last.equals("priority")) {
            mPriority = Integer.valueOf(sr.getText());
          }
          break;
        }

        case XMLStreamConstants.END_ELEMENT:
        {
          last = "";
          break;
        }
      }

      sr.next();
    }
  }

  public String getFrom()
  {
    return mFrom;
  }
}
