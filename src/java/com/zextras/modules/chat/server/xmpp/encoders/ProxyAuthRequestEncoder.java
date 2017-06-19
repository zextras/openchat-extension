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

package com.zextras.modules.chat.server.xmpp.encoders;

import com.ctc.wstx.stax.WstxOutputFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.operations.ProxyAuthentication;
import org.codehaus.stax2.XMLStreamProperties;
import org.codehaus.stax2.XMLStreamWriter2;
import org.openzal.zal.Account;
import org.openzal.zal.AuthProvider;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;

@Singleton
public class ProxyAuthRequestEncoder
{
  private final AuthProvider      mAuthProvider;
  private final WstxOutputFactory mFactory;

  @Inject
  public ProxyAuthRequestEncoder(AuthProvider authProvider)
  {
    mAuthProvider = authProvider;
    mFactory = new WstxOutputFactory();
    mFactory.setProperty(XMLStreamProperties.XSP_NAMESPACE_AWARE, Boolean.TRUE);
    mFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
  }

  public String buildInitialProxyPayload(
    Account account,
    boolean isUsingSSL,
    ProxyAuthentication.AuthType authType,
    String eventId,
    String resource
  )
  {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try
    {
      XMLStreamWriter2 writer = (XMLStreamWriter2) mFactory.createXMLStreamWriter(outputStream);
      writer.writeStartElement("proxyauth");
      writer.writeAttribute("authtoken", mAuthProvider.createAuthTokenForAccount(account).getEncoded());
      writer.writeAttribute("authtype", authType.toString());
      if (ProxyAuthentication.AuthType.IQ.equals(authType) && eventId != null && !eventId.isEmpty())
      {
        writer.writeAttribute("id", eventId);
      }
      if (ProxyAuthentication.AuthType.IQ.equals(authType) && resource != null && !resource.isEmpty())
      {
        writer.writeAttribute("resource", resource);
      }
      if (isUsingSSL)
      {
        writer.writeAttribute("usingssl", "true");
      }
      writer.writeEndElement();
      writer.flush();
    }
    catch (XMLStreamException e)
    {
      ChatLog.log.err(e.getMessage());
    }

    return outputStream.toString();
  }
}
