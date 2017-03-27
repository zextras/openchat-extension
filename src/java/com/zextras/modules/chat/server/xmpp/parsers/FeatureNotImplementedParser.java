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

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

public class FeatureNotImplementedParser extends XmppParser
{
  private String        mRequestId;
  private String        mOriginalReceiver;
  private IQRequestType mRequestType;

  public FeatureNotImplementedParser(InputStream xmlInputStream, SchemaProvider schemaProvider)
  {
    super("", xmlInputStream, schemaProvider);

    mRequestId = null;
    mOriginalReceiver = null;
    mRequestType = null;
  }

  @Override
  public void parse() throws XMLStreamException
  {
    XMLStreamReader2 sr = getStreamReader();

    if (validate()) {
      sr.validateAgainst(getDefaultSchema());
    }

    while (sr.hasNext())
    {
      if (XMLStreamConstants.START_ELEMENT == sr.next())
      {
        mRequestId = emptyStringWhenNull(sr.getAttributeValue(null, "id"));
        mRequestType = IQRequestType.fromString(sr.getAttributeValue(null, "type"));
        mOriginalReceiver = emptyStringWhenNull(sr.getAttributeValue(null, "to"));
        return;
      }
    }
  }

  public IQRequestType getRequestType()
  {
    return mRequestType;
  }

  public String getRequestId()
  {
    return mRequestId;
  }

  public String getOriginalReceiver()
  {
    return mOriginalReceiver;
  }
}
