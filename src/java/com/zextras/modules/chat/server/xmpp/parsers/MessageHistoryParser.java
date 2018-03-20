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
import com.zextras.lib.Optional;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.events.EventMessage;
import com.zextras.modules.chat.server.events.EventMessageHistory;
import com.zextras.modules.chat.server.events.EventSharedFile;
import com.zextras.modules.chat.server.events.FileInfo;
import com.zextras.modules.chat.server.events.TargetType;
import com.zextras.modules.chat.server.xmpp.encoders.EventMessageHistoryEncoder;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.apache.commons.lang3.StringEscapeUtils;
import org.codehaus.stax2.XMLStreamReader2;
import org.jetbrains.annotations.NotNull;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.text.ParseException;

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
  private String mBody;
  private String mSender;
  private Event  mEvent;
  private String mTimestamp;

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
    mEvent = null;
    mTimestamp = "0";
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
              mTimestamp = sr.getAttributeValue("", "timestamp");
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
              String messageFrom = emptyStringWhenNull(sr.getAttributeValue("", "from"));
              String messageTo = emptyStringWhenNull(sr.getAttributeValue("", "to"));
              String targetType = sr.getAttributeValue("", "type");
              parseMessage(messageFrom, messageTo, targetType, sr);
              continue;
            }
            case "shared-file":
            {
              EventId id = EventId.fromString(sr.getAttributeValue(null, "id"));
              Target target = new Target(new SpecificAddress(sr.getAttributeValue(null, "to")));
              SpecificAddress sender = new SpecificAddress(sr.getAttributeValue(null, "from"));
              String originalTargetString = sr.getAttributeValue(null, "original-target");
              TargetType targetType = TargetType.fromString(sr.getAttributeValue(null, "target-type"));
              SpecificAddress originalTarget = null;
              if (originalTargetString != null)
              {
                originalTarget = new SpecificAddress(originalTargetString);
              }
              parseSharedFile(id, target, sender, targetType, originalTarget, sr);
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

  private void parseSharedFile(EventId id, Target target, SpecificAddress sender, TargetType targetType, SpecificAddress originalTarget, XMLStreamReader2 sr) throws XMLStreamException
  {
    FileInfo fileInfo = null;
    while (sr.hasNext())
    {
      switch( sr.getEventType() )
      {
        case XMLStreamReader.START_ELEMENT:
        {
          if ("file-info".equals(sr.getLocalName()))
          {
            fileInfo = new FileInfo(
              sr.getAttributeValue(null, "owner-id"),
              sr.getAttributeValue(null, "share-id"),
              sr.getAttributeValue(null, "filename"),
              Long.valueOf(sr.getAttributeValue(null, "file-size")),
              sr.getAttributeValue(null, "content-type")
            );
          }
        }
      }
      sr.next();
    }

    try
    {
      mEvent = new EventSharedFile(
        id,
        sender,
        new Optional<SpecificAddress>(originalTarget),
        target,
        Long.valueOf(mMessageStamp),
        fileInfo,
        targetType
      );
    }
    catch (NumberFormatException e)
    {
      throw new XMLStreamException(e);
    }
  }

  private void parseMessage(String messageFrom, String messageTo, String targetTypeString, XMLStreamReader2 sr) throws XMLStreamException
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
    TargetType targetType;
    if (TargetType.GroupChat.name().equalsIgnoreCase(targetTypeString))
    {
      targetType = TargetType.GroupChat;
    }
    else if (TargetType.Channel.name().equalsIgnoreCase(targetTypeString))
    {
      targetType = TargetType.Channel;
    }
    else if (TargetType.Space.name().equalsIgnoreCase(targetTypeString))
    {
      targetType = TargetType.Space;
    }
    else
    {
      targetType = TargetType.Chat;
    }

    try
    {
      mEvent = new EventMessage(
        EventId.fromString(mMessageId),
        new SpecificAddress(messageFrom),
        new Target(new SpecificAddress(messageTo)),
        mBody,
        Long.valueOf(mMessageStamp),
        targetType
      );
    }
    catch (NumberFormatException e)
    {
      throw new XMLStreamException(e);
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

  public long getTimestamp()
  {
    return Long.valueOf(mTimestamp);
  }

  public Event getEvent()
  {
    return mEvent;
  }

  @NotNull
  public String getBody()
  {
    return mBody;
  }

}
