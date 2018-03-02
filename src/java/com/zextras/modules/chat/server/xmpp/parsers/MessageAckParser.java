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

import com.zextras.lib.DateUtils;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamReader2;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.text.ParseException;

public class MessageAckParser extends XmppParser
{
  private String mTo;
  private String mFrom;
  private String mMessageId;
  private String mMessageTimestamp;
  private String mId;

  public MessageAckParser(InputStream xmlInput, SchemaProvider schemaProvider)
  {
    super("jabber-client.xsd", xmlInput, schemaProvider);
    mMessageTimestamp = "";
  }

/*
  <message xmlns=\"jabber:client\" from=\"target@example.com\" to=\"sender@example.com\" id=\"01d03131-abd1-4bd8-a058-860cdf18ee83\">
    <received xmlns=\"urn:xmpp:receipts\" id=\"message-id\"/>
  </message>";
*/
  @Override
  public void parse() throws XMLStreamException
  {
    XMLStreamReader2 sr = getStreamReader();

    if (validate())
    {
      sr.validateAgainst(getDefaultSchema());
    }

    while( sr.hasNext() )
    {
      sr.next();

      switch ( sr.getEventType() )
      {
        case XMLStreamReader.START_ELEMENT:
        {
          if( sr.getName().getLocalPart().equals("received") )
          {
            mMessageId = emptyStringWhenNull(sr.getAttributeValue(null,"id"));
          }

          if( sr.getName().getLocalPart().equals("message") )
          {
            mId = emptyStringWhenNull(sr.getAttributeValue(null, "id"));
            mTo = emptyStringWhenNull(sr.getAttributeValue(null, "to"));
            mFrom = emptyStringWhenNull(sr.getAttributeValue(null, "from"));
            mMessageTimestamp = emptyStringWhenNull(sr.getAttributeValue(null, "timestamp"));
          }
        }
      }
    }
  }

  public String getId()
  {
    return mId;
  }

  public String getTo()
  {
    return mTo;
  }

  public String getFrom()
  {
    return mFrom;
  }

  public String getMessageId()
  {
    return mMessageId;
  }

  public long getMessageTimestamp() // not really xmmp standard
  {
    try
    {
      return DateUtils.parseUTCDate(mMessageTimestamp);
    }
    catch (ParseException e)
    {
      return System.currentTimeMillis();
    }
  }
}
