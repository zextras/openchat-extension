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
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

public class IQRosterParser extends XmppParser
{
  private String        mFrom         = "";
  private String        mRequestId    = "";
  private String        mJID          = "";
  private String        mSubscription = "";
  private IQRequestType mType         = IQRequestType.UNKNOWN;
  private String        mGroup        = "";
  private String        mItemName     = "";

  public IQRosterParser(InputStream xmlInput, SchemaProvider schemaProvider)
  {
    super("roster.xsd", xmlInput, schemaProvider);
  }

  @Override
  public void parse() throws XMLStreamException
  {
    XMLStreamReader2 sr = getStreamReader();
    String lastTag = "";

    while (sr.hasNext())
    {
      sr.next();
      if(sr.getEventType() == XMLStreamReader.START_ELEMENT)
      {
        if(sr.getLocalName().equals("iq"))
        {
          lastTag = "iq";
          mFrom = emptyStringWhenNull(sr.getAttributeValue(null,"from"));
          mRequestId = emptyStringWhenNull(sr.getAttributeValue(null,"id"));
          mType = IQRequestType.fromString(sr.getAttributeValue(null, "type"));
          continue;
        }

        if(sr.getLocalName().equals("item"))
        {
          lastTag = "item";
          mJID = emptyStringWhenNull(sr.getAttributeValue(null,"jid"));
          mSubscription = emptyStringWhenNull(sr.getAttributeValue(null, "subscription"));
          mItemName = emptyStringWhenNull(sr.getAttributeValue(null, "name"));
          continue;
        }

        if(sr.getLocalName().equals("group"))
        {
          lastTag = "group";
        }
      }
      else if(sr.getEventType() == XMLStreamReader.CHARACTERS)
      {
         if(lastTag.equals("group"))
         {
           mGroup = emptyStringWhenNull(sr.getText());
         }
      }
      else if(sr.getEventType() == XMLStreamReader.END_ELEMENT)
      {
         lastTag = "";
      }
    }
  }


  public String getFrom()
  {
    return mFrom;
  }

  public String getRequestId()
  {
    return mRequestId;
  }

  public IQRequestType getType()
  {
    return mType;
  }

  public String getJID()
  {
    return mJID;
  }

  public String getSubscription()
  {
    return mSubscription;
  }

  public String getGroup()
  {
    return mGroup;
  }

  public String getItemName()
  {
    return mItemName;
  }
}
