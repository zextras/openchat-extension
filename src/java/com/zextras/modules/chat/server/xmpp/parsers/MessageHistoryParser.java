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

import com.zextras.modules.chat.server.events.EventMessageHistory;
import com.zextras.modules.chat.server.xmpp.encoders.EventMessageHistoryEncoder;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.apache.commons.lang3.StringEscapeUtils;
import org.codehaus.stax2.XMLStreamReader2;
import org.jetbrains.annotations.NotNull;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

/**
 * @see EventMessageHistory
 * @see EventMessageHistoryEncoder
 * @see com.zextras.modules.chat.server.xmpp.parsers.MessageHistoryParser
 */
public class MessageHistoryParser extends XmppParser
{
  private String mId;
  private String mTo;
  private String mQueryId;
  private String mMessageId;
  private String mMessageStamp;
  private String mMessageTo;
  private String mMessageFrom;
  private String mBody;
  private String mSender;

  public MessageHistoryParser(
    InputStream xmlInput,
    SchemaProvider schemaProvider
  )
  {
    super("jabber-client.xsd", xmlInput, schemaProvider);
    mId = "";
    mSender = "";
    mTo = "";
    mQueryId = "";
    mMessageId = "";
    mMessageStamp = "";
    mMessageTo = "";
    mMessageFrom = "";
    mBody = "";
  }

/*
<message id='aeb213' to='juliet@capulet.lit/chamber'>
  <result xmlns='urn:xmpp:mam:2' queryid='f27' id='28482-98726-73623'>
    <forwarded xmlns='urn:xmpp:forward:0'>
      <delay xmlns='urn:xmpp:delay' stamp='2010-07-10T23:08:25Z'/>
      <message xmlns='jabber:client' from="witch@shakespeare.lit" to="macbeth@shakespeare.lit">
        <body>Hail to thee</body>
      </message>
    </forwarded>
  </result>
</message>
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
            case "message":
            {
              mSender = emptyStringWhenNull(sr.getAttributeValue("", "from"));
              mTo = emptyStringWhenNull(sr.getAttributeValue("", "to"));
              mId = emptyStringWhenNull(sr.getAttributeValue("", "id"));
              break;
            }
            case "result":
            {
              mMessageId = emptyStringWhenNull(sr.getAttributeValue("", "id"));
              mQueryId = emptyStringWhenNull(sr.getAttributeValue("", "queryid"));
              parseResult(sr);
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

  private void parseResult(XMLStreamReader2 sr) throws XMLStreamException
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
            case "forwarded":
            {
              parseForwarded(sr);
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

  private void parseForwarded(XMLStreamReader2 sr) throws XMLStreamException
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
            case "delay":
            {
              mMessageStamp = emptyStringWhenNull(sr.getAttributeValue("", "stamp"));
              break;
            }
            case "message":
            {
              mMessageFrom = emptyStringWhenNull(sr.getAttributeValue("", "from"));
              mMessageTo = emptyStringWhenNull(sr.getAttributeValue("", "to"));
              parseMessage(sr);
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

  private void parseMessage(XMLStreamReader2 sr) throws XMLStreamException
  {
    String tag = "";
    while (sr.hasNext())
    {
      switch (sr.getEventType())
      {
        case XMLStreamReader.START_ELEMENT:
        {
          tag = sr.getLocalName();
          break;
        }
        case XMLStreamConstants.CHARACTERS:
        {
          switch (tag)
          {
            case "body":
            {
              mBody = emptyStringWhenNull(StringEscapeUtils.unescapeXml(sr.getText()));
              break;
            }
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
  public String getTo()
  {
    return mTo;
  }

  @NotNull
  public String getSender()
  {
    return mSender;
  }

  @NotNull
  public String getQueryId()
  {
    return mQueryId;
  }

  @NotNull
  public String getMessageId()
  {
    return mMessageId;
  }

  @NotNull
  public String getMessageStamp()
  {
    return mMessageStamp;
  }

  @NotNull
  public String getMessageTo()
  {
    return mMessageTo;
  }

  @NotNull
  public String getMessageFrom()
  {
    return mMessageFrom;
  }

  @NotNull
  public String getBody()
  {
    return mBody;
  }

}
