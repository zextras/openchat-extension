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

package com.zextras.modules.chat.server.xmpp;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class StanzaRecognizerImpl implements StanzaRecognizer
{
  @Override
  public StanzaType recognize(String xml) throws XMLStreamException
  {
    RewindableXmlStreamReader sr = new RewindableXmlStreamReader(xml);
    StanzaType stanzaType = StanzaType.Unknown;

    while( sr.hasNext() )
    {
      sr.next();
      if( sr.getEventType() == XMLStreamReader.START_ELEMENT )
      {
        if (sr.getLocalName().equals("presence"))
        {
          stanzaType = getTypePresence(sr);
          break;
        }

        if (sr.getLocalName().equals("message"))
        {
          stanzaType = getTypeMessage(sr);
          break;
        }

        if(sr.getLocalName().equals("auth"))
        {
          stanzaType = StanzaType.SASLAuth;
          break;
        }

        if(sr.getLocalName().equals("stream"))
        {
          stanzaType = StanzaType.Stream;
          break;
        }

        if(sr.getLocalName().equals("starttls"))
        {
          stanzaType = StanzaType.StartTLS;
          break;
        }

        if(sr.getLocalName().equals("proxyauth"))
        {
          stanzaType = StanzaType.ProxyAuth;
          break;
        }


        if(sr.getLocalName().equals("iq"))
        {
          stanzaType = getTypeIQ(sr);
          break;
        }
      }
    }

    return stanzaType;
  }

  private StanzaType getTypePresence(RewindableXmlStreamReader sr ) throws XMLStreamException
  {
    if(isPresenceProbe(sr))
    {
      return StanzaType.StatusProbe;
    }
    return StanzaType.Presence;
  }

  private boolean isPresenceProbe( RewindableXmlStreamReader sr )
  {
    String type = emptyStringWhenNull(sr.getAttributeValue(null,"type"));
    return type.equals("probe");
  }

  private String emptyStringWhenNull(String value)
  {
    if (value == null) {
      return "";
    } else {
      return value;
    }
  }

  private StanzaType getTypeMessage(RewindableXmlStreamReader sr) throws XMLStreamException
  {
    if(isMessageAck(sr))
    {
      return StanzaType.MessageAck;
    }

    return StanzaType.Message;
  }

  private boolean isMessageAck( RewindableXmlStreamReader sr )
    throws XMLStreamException
  {
    return isContained(
      "received",                    // LocalName
      "xmlns",                       // AttributeName
      "urn:xmpp:receipts",           // expected Value
      XMLStreamReader.START_ELEMENT, // Type of Event
      sr
    );

  }

  public boolean isContained(
    String name,
    String nameSpace,
    String valueExpected,
    int compareWith,
    RewindableXmlStreamReader sr
  )
    throws XMLStreamException
  {
    sr.rewind();

    while( sr.hasNext() )
    {
      sr.next();

      if( sr.getEventType() == compareWith && sr.getLocalName().equals(name))
      {
        if(!emptyStringWhenNull(sr.getNamespaceURI()).equals(valueExpected))
        {
          return emptyStringWhenNull(sr.getAttributeValue(null, nameSpace)).equals(valueExpected);
        }
        return emptyStringWhenNull(sr.getNamespaceURI()).equals(valueExpected);
      }
    }
    return false;
  }

  private StanzaType getTypeIQ(RewindableXmlStreamReader sr) throws XMLStreamException
  {
    if(containsElement("ping", XMLStreamReader.START_ELEMENT, sr))
    {
      return StanzaType.Ping;
    }

    if(containsElement("session", XMLStreamReader.START_ELEMENT, sr))
    {
      return StanzaType.Session;
    }

    if(containsElement("bind", XMLStreamReader.START_ELEMENT, sr))
    {
      return StanzaType.Bind;
    }

    if(isContained(
      "query",                                          // LocalName
      "xmlns",                                          // AttributeName
      "jabber:iq:roster",                               // expected Value
      XMLStreamReader.START_ELEMENT,                    // Type of Event
      sr
    ))
    {
      return StanzaType.IQRoster;
    }

    if(isContained(
      "query",                                            // LocalName
      "xmlns",                                            // AttributeName
      "http://jabber.org/protocol/disco#info",            // expected Value
      XMLStreamReader.START_ELEMENT,                      // Type of Event
      sr
    ))
    {
      return StanzaType.Discovery;
    }

    if(isContained(
      "query",                                            // LocalName
      "xmlns",                                            // AttributeName
      "jabber:iq:privacy",                                // expected Value
      XMLStreamReader.START_ELEMENT,                      // Type of Event
      sr
    ))
    {
      return StanzaType.Privacy;
    }

    if(isContained(
      "query",                                            // LocalName
      "xmlns",                                            // AttributeName
      "jabber:iq:auth",                                   // expected Value
      XMLStreamReader.START_ELEMENT,                      // Type of Event
      sr
    ))
    {
      return StanzaType.IQAuth;
    }

    if(isContained(
      "query",                                            // LocalName
      "xmlns",                                            // AttributeName
      "jabber:iq:last",                                   // expected Value
      XMLStreamReader.START_ELEMENT,                      // Type of Event
      sr
    ))
    {
      return StanzaType.LastActivity;
    }

    if(isContained(
      "query",                                            // LocalName
      "xmlns",                                            // AttributeName
      "jabber:iq:last",                                   // expected Value
      XMLStreamReader.START_ELEMENT,                      // Type of Event
      sr
    ))
    {
      return StanzaType.LastActivity;
    }

    return StanzaType.UnknownIq;
  }

  public boolean containsElement(
    String name,
    int compareWith,
    RewindableXmlStreamReader sr
  )
    throws XMLStreamException
  {
    sr.rewind();

    while( sr.hasNext() )
    {
      if( sr.getEventType() == compareWith)
      {
        if(sr.getLocalName().equals(name))
        {
          return true;
        }
      }
      sr.next();

    }
    return false;
  }
}
