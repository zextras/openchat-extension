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

import com.zextras.modules.chat.server.operations.ProxyAuthentication;
import org.codehaus.stax2.XMLStreamReader2;
import org.openzal.zal.AuthProvider;
import org.openzal.zal.AuthToken;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;

public class ProxyAuthParser extends XmppParser
{
  private final AuthProvider mAuthProvider;
  private       String       mAuthToken;
  private       String       mAuthType;
  private       String       mRequestId;
  private       String       mResource;
  private boolean mUsingSSL;

  public ProxyAuthParser(ByteArrayInputStream xmlInputStream, AuthProvider authProvider)
  {
    super(null, xmlInputStream, null);
    mAuthProvider = authProvider;
  }

  @Override
  public void parse() throws XMLStreamException
  {
    XMLStreamReader2 sr = getStreamReader();

    while (sr.hasNext())
    {
      sr.next();
      switch (sr.getEventType()) {
        case XMLStreamReader2.START_ELEMENT: {
          if (sr.getLocalName().equals("proxyauth")) {
            mAuthToken = emptyStringWhenNull(sr.getAttributeValue(null, "authtoken"));
            mAuthType = emptyStringWhenNull(sr.getAttributeValue(null, "authtype"));
            mRequestId = emptyStringWhenNull(sr.getAttributeValue(null,"id"));
            mResource = emptyStringWhenNull(sr.getAttributeValue(null, "resource"));
            String usingSSL = sr.getAttributeValue(null, "usingssl");
            mUsingSSL = usingSSL != null && usingSSL.equalsIgnoreCase("true");
          }
        }
      }
    }
  }

  public AuthToken getAuthToken()
  {
    return mAuthProvider.decodeAuthToken(mAuthToken);
  }

  public ProxyAuthentication.AuthType getAuthType()
  {
    return ProxyAuthentication.AuthType.valueOf(mAuthType);
  }

  public String getEventId()
  {
    return mRequestId;
  }

  public String getResource()
  {
    return mResource;
  }

  public boolean isUsingSSL()
  {
    return mUsingSSL;
  }
}
