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

public class MessageParser extends XmppParser
{
  private boolean mActive   = false;
  private boolean mInactive = false;
  private boolean mGone     = false;
  private String  mFrom     = "";

  public boolean isComposing()
  {
    return mComposing;
  }

  private boolean mComposing;

  public String getTo()
  {
    return mTo;
  }

  public String getType()
  {
    return mType;
  }

  public String getId()
  {
    return mId;
  }

  public String getBody()
  {
    return mBody;
  }

  private String mTo   = "";
  private String mType = "";
  private String mId   = "";
  private String mBody = "";

  public MessageParser(InputStream xmlInput, SchemaProvider schemaProvider)
  {
    super("jabber-client.xsd", xmlInput, schemaProvider);
  }


/*
<message type="chat" to="admin@example.com" id="73">
  <x xmlns="jabber:x:event">
    <offline/>
    <composing/>
    <delivered/>
    <displayed/>
  </x>
  <active xmlns="http://jabber.org/protocol/chatstates"/>
  <body>hello</body>
</message>

<message type="chat" to="admin@example.com" id="aac5a">
  <composing xmlns="http://jabber.org/protocol/chatstates"/>
</message>
*/

  @Override
  public void parse() throws XMLStreamException
  {
    XMLStreamReader2 sr = getStreamReader();

    if (validate()) {
      sr.validateAgainst(getDefaultSchema());
    }

    while( sr.hasNext() )
    {
      switch ( sr.getEventType() )
      {
        case XMLStreamReader.START_ELEMENT:
        {
          mType = emptyStringWhenNull(sr.getAttributeValue(null,"type"));
          mTo = emptyStringWhenNull(sr.getAttributeValue(null,"to"));
          mId = emptyStringWhenNull(sr.getAttributeValue(null,"id"));
          mFrom = emptyStringWhenNull(sr.getAttributeValue(null,"from"));
          parseMessage(sr);
          return;
        }
      }

      sr.next();
    }
  }

  private void parseMessage( XMLStreamReader2 sr  ) throws XMLStreamException
  {
    String last = "";
    while( sr.hasNext() )
    {
      switch ( sr.getEventType() )
      {
        case XMLStreamReader.START_ELEMENT:
        {
          last = sr.getLocalName().toLowerCase();

          if( last.equals("x") ) {
            sr.skipElement();
            break;
          }

          if( last.equals("composing") ) {
            mComposing = true;
          }
          if( last.equals("active") ) {
            mActive = true;
          }
          if( last.equals("gone") ) {
            mGone = true;
          }
          if( last.equals("inactive") ) {
            mInactive = true;
          }
          break;
        }

        case XMLStreamReader.END_ELEMENT:
        {
          last = "";
          break;
        }

        case XMLStreamReader.CHARACTERS:
        {
          if( last.equals("body")) {
            mBody = sr.getText();
          }
          if(last.equals("x")) {
            //parseMessageEvent();
          }
          break;
        }
      }

      sr.next();
    }
  }

  public boolean isActive()
  {
    return mActive;
  }

  public boolean isGone()
  {
    return mGone;
  }

  public String getFrom()
  {
    return mFrom;
  }

  public boolean isInactive()
  {
    return mInactive;
  }
}
