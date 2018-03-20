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

import com.zextras.modules.chat.server.events.EventMessageHistoryLast;
import com.zextras.modules.chat.server.xmpp.encoders.EventMessageHistoryLastEncoder;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamReader2;
import org.jetbrains.annotations.NotNull;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

/**
 * @see EventMessageHistoryLast
 * @see EventMessageHistoryLastEncoder
 * @see com.zextras.modules.chat.server.xmpp.parsers.MessageHistoryLastParser
 */
public class MessageHistoryLastParser extends XmppParser
{
  private String mId;
  private String mSender;
  private String mTo;
  private String mQueryId;
  private String mFirst;
  private String mLast;
  private String mTimestamp;
  private String mCount;

  public MessageHistoryLastParser(
    InputStream xmlInput,
    SchemaProvider schemaProvider
  )
  {
    super("jabber-client.xsd", xmlInput, schemaProvider);
    mId = "";
    mTo = "";
    mQueryId = "";
    mFirst = "";
    mLast = "";
    mSender = "";
    mTimestamp = "0";
    mCount = "";
  }

/*
<iq type='result' id='juliet1'>
  <fin xmlns='urn:xmpp:mam:2'>
    <set xmlns='http://jabber.org/protocol/rsm'>
      <first index='0'>28482-98726-73623</first>
      <last>09af3-cc343-b409f</last>
    </set>
  </fin>
</iq>
*/

  public void parse() throws XMLStreamException
  {
    XMLStreamReader2 sr = getStreamReader();
    String tag = "";
    while (sr.hasNext())
    {
      switch (sr.getEventType())
      {
        case XMLStreamReader.START_ELEMENT:
        {
          tag = sr.getLocalName();

          switch (tag)
          {
            case "iq":
            {
              mSender = emptyStringWhenNull(sr.getAttributeValue("", "from"));
              mTo = emptyStringWhenNull(sr.getAttributeValue("", "to"));
              mId = emptyStringWhenNull(sr.getAttributeValue("", "id"));
              break;
            }
            case "fin":
            {
              mQueryId = emptyStringWhenNull(sr.getAttributeValue("", "queryid"));
              parseFin(sr);
              continue;
            }
          }
          break;
        }
        case XMLStreamConstants.CHARACTERS:
        {
          break;
        }
        case XMLStreamConstants.END_ELEMENT:
        {
          tag = "";
          break;
        }
      }
      sr.next();
    }
  }

  private void parseFin(XMLStreamReader2 sr) throws XMLStreamException
  {
    String tag = "";
    while (sr.hasNext())
    {
      switch (sr.getEventType())
      {
        case XMLStreamReader.START_ELEMENT:
        {
          tag = sr.getLocalName();

          switch (tag)
          {
            case "set":
            {
              parseSet(sr);
              continue;
            }
          }
          break;
        }
        case XMLStreamConstants.CHARACTERS:
        {
          break;
        }
        case XMLStreamConstants.END_ELEMENT:
        {
          tag = "";
          break;
        }
      }
      sr.next();
    }
  }

  private void parseSet(XMLStreamReader2 sr) throws XMLStreamException
  {
    String tag = "";
    while (sr.hasNext())
    {
      switch (sr.getEventType())
      {
        case XMLStreamReader.START_ELEMENT:
        {
          tag = sr.getLocalName();
          switch (tag)
          {
            case "last":
              mTimestamp = emptyStringWhenNull(sr.getAttributeValue("", "stamp"));
              break;
          }
          break;
        }
        case XMLStreamConstants.CHARACTERS:
        {
          switch (tag)
          {
            case "first":
              mFirst = emptyStringWhenNull(sr.getText());
              break;
            case "last":
              mLast = emptyStringWhenNull(sr.getText());
              break;
            case "count":
              mCount = emptyStringWhenNull(sr.getText());
              break;
          }
          break;
        }
        case XMLStreamConstants.END_ELEMENT:
        {
          tag = "";
          break;
        }
      }
      sr.next();
    }
  }

  @NotNull
  public String getId()
  {
    return mId;
  }

  @NotNull
  public String getSender()
  {
    return mSender;
  }

  @NotNull
  public String getTo()
  {
    return mTo;
  }

  @NotNull
  public String getQueryId()
  {
    return mQueryId;
  }

  @NotNull
  public String getFirst()
  {
    return mFirst;
  }

  @NotNull
  public String getLast()
  {
    return mLast;
  }

  @NotNull
  public long getTimestamp()
  {
    return Long.valueOf(mTimestamp);
  }

  @NotNull
  public String getCount()
  {
    return mCount;
  }
}
