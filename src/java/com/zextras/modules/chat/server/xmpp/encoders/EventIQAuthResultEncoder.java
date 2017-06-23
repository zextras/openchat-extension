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

import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.EventIQAuthResult;
import com.zextras.modules.chat.server.xmpp.XmppAuthentication;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

public class EventIQAuthResultEncoder extends XmppEncoder
{
  private final EventIQAuthResult mEvent;
/*
<iq type='result' id='auth1' xmlns="jabber:client">
  <query xmlns='jabber:iq:auth'>
    <username/>
    <password/>
    <digest/>
    <resource/>
  </query>
</iq>
*/

  public EventIQAuthResultEncoder(
    EventIQAuthResult event,
    SchemaProvider schemaProvider
  )
  {
    super("jabber-client.xsd", schemaProvider);
    mEvent = event;
  }

/*
 <error code='401' type='auth'>
    <not-authorized xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>
  </error>
*/

  @Override
  public void encode(OutputStream outputStream, SpecificAddress target)
    throws XMLStreamException
  {
    XMLStreamWriter2 sr = getStreamWriter(outputStream);
    if( validate() ) {
      //no validation available for legacy authentication
    }

    sr.setPrefix("","jabber:client");
    sr.writeStartElement("","iq","jabber:client");
    sr.writeAttribute("id",mEvent.getId().toString());

    switch( mEvent.getAuthStatus() )
    {
      case SUCCESS:
        sr.writeAttribute("type","result");
        break;

      case INVALID_CREDENTIALS:
        sr.writeAttribute("type","error");
        encodeFailed(sr,"401");
        break;

      case SERVER_ERROR:
        sr.writeAttribute("type","error");
        encodeFailed(sr,"500");
        break;

      default:
      case NOT_REQUESTED:
        sr.writeAttribute("type","result");
        replyAvailable(sr);
        break;
    }

    sr.writeEndElement();
    sr.close();
  }

  private void encodeFailed(XMLStreamWriter2 sr, String errorCode) throws XMLStreamException
  {
    sr.writeStartElement("jabber:client","error");
    sr.writeAttribute("code", errorCode);
    sr.writeAttribute("type","auth");
    sr.writeEmptyElement("","not-authorized","urn:ietf:params:xml:ns:xmpp-stanzas");
    sr.writeEndElement();
  }

  private void replyAvailable(XMLStreamWriter2 sr) throws XMLStreamException
  {
    sr.setPrefix("","jabber:iq:auth");
    sr.writeStartElement("","query","jabber:iq:auth");
    for(XmppAuthentication auth : mEvent.getAvailableAuthentications() )
    {
      if( auth.toString().equalsIgnoreCase("PLAIN") ) {
        sr.writeEmptyElement("username");
        sr.writeEmptyElement("password");
        sr.writeEmptyElement("resource");
      }
    }
    sr.writeEndElement();
  }
}
