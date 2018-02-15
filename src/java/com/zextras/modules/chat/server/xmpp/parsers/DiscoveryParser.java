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
import com.zextras.modules.chat.server.events.EventDiscovery;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamReader2;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class DiscoveryParser extends XmppParser
{
  private String mType;
  private String mId;
  private String mTarget;
  private String mQuery;
  private String mSender;
  private Collection<String>          mFeatures = Collections.<String>emptyList();
  private List<EventDiscovery.Result> mResults  = Collections.<EventDiscovery.Result>emptyList();

  public DiscoveryParser(InputStream xmlInput, SchemaProvider schemaProvider)
  {
    super("disco-info.xsd", xmlInput, schemaProvider);
  }

  // <iq type='get' id='purple841b5737' to='example.com'><query xmlns='http://jabber.org/protocol/disco#items'/></iq>

  @Override
  public void parse() throws XMLStreamException
  {
    XMLStreamReader2 sr = getStreamReader();

    if (validate())
    {
      sr.validateAgainst(getDefaultSchema());
    }

    while (sr.hasNext())
    {
      switch (sr.next())
      {
        case XMLStreamReader2.START_ELEMENT:
        {
          if (sr.getLocalName().equals("iq"))
          {
            mType = sr.getAttributeValue(null, "type");
            mId = sr.getAttributeValue(null, "id");
            mTarget = sr.getAttributeValue(null, "to");
            mSender = sr.getAttributeValue(null, "from");
          }
          else if (sr.getLocalName().equals("query"))
          {
            mQuery = sr.getNamespaceURI();
            parseItems(sr);
          }
          else if (sr.getLocalName().equalsIgnoreCase("feature"))
          {
            mFeatures = addString(mFeatures, sr.getAttributeValue("", "var"));
          }
        }
      }
    }
  }

  private void parseItems(XMLStreamReader2 sr) throws XMLStreamException
  {
    while (sr.hasNext())
    {
      switch (sr.next())
      {
        case XMLStreamReader2.START_ELEMENT:
        {
          if (sr.getLocalName().equals("item"))
          {
            if (mResults.isEmpty())
            {
              mResults = new LinkedList<>();
            }

            mResults.add(
              new EventDiscovery.Result(
                new SpecificAddress(sr.getAttributeValue(null, "jid")),
                sr.getAttributeValue(null, "name")
              )
            );
          }
          break;
        }

        case XMLStreamReader2.END_ELEMENT:
        {
          if (sr.getLocalName().equals("query"))
          {
            return;
          }
        }
      }
    }
  }

  public String getSender()
  {
    return mSender;
  }

  public String getType()
  {
    return mType;
  }

  public String getId()
  {
    return mId;
  }

  public String getTarget()
  {
    return mTarget;
  }

  public String getQuery()
  {
    return mQuery;
  }

  public Collection<String> getFeatures()
  {
    return mFeatures;
  }

  public List<EventDiscovery.Result> getResults()
  {
    return mResults;
  }
}
