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

import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamReader2;
import org.jetbrains.annotations.NotNull;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

/**
 * @see com.zextras.modules.chat.server.xmpp.encoders.EventIQQueryEncoder
 * @see com.zextras.modules.chat.server.events.EventIQQuery
 * @see com.zextras.modules.chat.server.xmpp.parsers.IQQueryXmppParser
 */
public class IQQueryXmppParser extends XmppParser
{
  private String mId;
  private String mTo;
  private String mQueryId;
  private String mNode;
  private String mWith;
  private String mStart;
  private String mEnd;
  private String mSender;

  public IQQueryXmppParser(
    InputStream xmlInput,
    SchemaProvider schemaProvider
  )
  {
    super("jabber-client.xsd", xmlInput, schemaProvider);
    mId = "";
    mTo = "";
    mQueryId = "";
    mNode = "";
    mWith = "";
    mStart = "";
    mEnd = "";
    mSender = "";
  }

/*
<iq type='set' id='juliet1'>
  <query xmlns='urn:xmpp:mam:2'>
    <x xmlns='jabber:x:data' type='submit'>
      <field var='FORM_TYPE' type='hidden'>
        <value>urn:xmpp:mam:2</value>
      </field>
      <field var='with'>
        <value>juliet@capulet.lit</value>
      </field>
    </x>
  </query>
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
            case "query":
            {
              mQueryId = emptyStringWhenNull(sr.getAttributeValue("", "queryid"));
              parseQuery(sr);
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

  private void parseQuery(XMLStreamReader2 sr) throws XMLStreamException
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
            case "x":
            {
              parseX(sr);
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

  private void parseX(XMLStreamReader2 sr) throws XMLStreamException
  {
    String tag = "";
    String var = "";
    String type = "";
    while (sr.hasNext())
    {
      switch (sr.getEventType())
      {
        case XMLStreamReader.START_ELEMENT:
        {
          tag = sr.getLocalName();

          switch (tag)
          {
            case "field":
            {
              var = emptyStringWhenNull(sr.getAttributeValue("", "var"));
              type = emptyStringWhenNull(sr.getAttributeValue("", "type"));
              break;
            }
          }

          break;
        }
        case XMLStreamConstants.CHARACTERS:
        {
          switch(var)
          {
            case "with":
              mWith = emptyStringWhenNull(sr.getText());
              break;
            case "start":
              mStart = emptyStringWhenNull(sr.getText());
              break;
            case "end":
              mEnd = emptyStringWhenNull(sr.getText());
              break;
          }
          break;
        }
        case XMLStreamConstants.END_ELEMENT:
        {
          tag = "";
          var = "";
          type = "";
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
  public String getWith()
  {
    return mWith;
  }

  @NotNull
  public String getQueryId()
  {
    return mQueryId;
  }

  @NotNull
  public String getNode()
  {
    return mNode;
  }

  @NotNull
  public String getStart()
  {
    return mStart;
  }

  @NotNull
  public String getEnd()
  {
    return mEnd;
  }

  @NotNull
  public SpecificAddress getSender()
  {
    return new SpecificAddress(mSender);
  }
}
