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

import com.zextras.lib.JSONWriter;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.EventType;
import com.zextras.modules.chat.server.events.RoomType;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamReader2;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class PresenceParser extends XmppParser
{
  public static final String sProtocolZextrasStatus = "http://zextras.com/protocol/chat/status";

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

  private String                mShow                = "chat";
  private String                mFrom                = "";
  private String                mStatus              = "";
  private boolean               mMultiUserChat       = false;
  private int                   mPriority            = 0;
  private long                  mValidSince          = 0;
  private EventType             mGroupType           = EventType.Chat;
  private List<SpecificAddress> mMeetings            = new LinkedList<>();

  public final static String sProtocolMuc = "http://jabber.org/protocol/muc";

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
          mFrom = emptyStringWhenNull(sr.getAttributeValue(null,"from"));
          mTo = emptyStringWhenNull(sr.getAttributeValue(null,"to"));
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
          if( last.equals("x") )
          {
            if(sr.getNamespaceURI().equals(sProtocolZextrasStatus) )
            {
              mGroupType = EventType.fromString(sr.getAttributeValue(null,"groupType"));
              mValidSince = Long.valueOf(sr.getAttributeValue(null, "validSince"));
            }
            else
            {
              String xmlns = sr.getNamespaceURI();
              mMultiUserChat = mMultiUserChat || xmlns.startsWith(sProtocolMuc);
            }
          }

          if( last.equals("meeting") )
          {
            mMeetings.add(
              new SpecificAddress(sr.getAttributeValue(null, "jid"))
            );
          }
          break;
        }

        case XMLStreamReader.CHARACTERS:
        {
          if( last.equals("show")) {
            mShow = sr.getText();
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

  public boolean isMucPresence()
  {
    return mMultiUserChat;
  }

  public List<SpecificAddress> getMeetings()
  {
    return mMeetings;
  }

  public long getValidSince()
  {
    return mValidSince;
  }

  public EventType getGroupType()
  {
    return mGroupType;
  }
}
