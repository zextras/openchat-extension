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

package com.zextras.modules.chat.server.xmpp.encoders;

import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.EventIQQuery;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

/**
 * @see com.zextras.modules.chat.server.xmpp.encoders.EventIQQueryEncoder
 * @see com.zextras.modules.chat.server.events.EventIQQuery
 * @see com.zextras.modules.chat.server.xmpp.parsers.IQQueryXmppParser
 */
public class EventIQQueryEncoder extends XmppEncoder
{
  private final EventIQQuery mEventIQQuery;

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

  public EventIQQueryEncoder(EventIQQuery eventIQQuery,
                             SchemaProvider schemaProvider)
  {
    super("jabber-client.xsd", schemaProvider);
    mEventIQQuery = eventIQQuery;
  }

  @Override
  public void encode(OutputStream outputStream, SpecificAddress target) throws XMLStreamException
  {
    XMLStreamWriter2 sw = getStreamWriter(outputStream);
    if (validate())
    {
      sw.validateAgainst(getDefaultSchema());
    }

    sw.writeStartElement("", "iq");
    sw.writeAttribute("type", "set");

      sw.writeAttribute("from", mEventIQQuery.getSender().toString()); // no-xmpp-standard
      sw.writeAttribute("to", target.toString()); // no-xmpp-standard
      sw.writeAttribute("id", mEventIQQuery.getId().toString());

      sw.writeStartElement("","query","urn:xmpp:mam:2" );
      if (!mEventIQQuery.getQueryId().isEmpty())
      {
        sw.writeAttribute("queryid", mEventIQQuery.getQueryId());
      }
      if (!StringUtils.isEmpty(mEventIQQuery.getWith()) ||
          !StringUtils.isEmpty(mEventIQQuery.getStart()) ||
          !StringUtils.isEmpty(mEventIQQuery.getEnd()))
      {
        sw.writeStartElement("","x","jabber:x:data" );
          sw.writeAttribute("type","submit");
          sw.writeStartElement("field");
            sw.writeAttribute("var","FORM_TYPE");
            sw.writeAttribute("type","hidden");
            sw.writeStartElement("value");
              sw.writeCharacters("urn:xmpp:mam:2");
            sw.writeEndElement();
          sw.writeEndElement();
          if (!StringUtils.isEmpty(mEventIQQuery.getWith()))
          {
            sw.writeStartElement("jabber:x:data", "field");
              sw.writeAttribute("var", "with");
              sw.writeStartElement("value");
                sw.writeCharacters(mEventIQQuery.getWith());
              sw.writeEndElement();
            sw.writeEndElement();
          }
          if (!StringUtils.isEmpty(mEventIQQuery.getStart()))
          {
            sw.writeStartElement("jabber:x:data", "field");
              sw.writeAttribute("var", "start");
              sw.writeStartElement("value");
                sw.writeCharacters(mEventIQQuery.getStart());
              sw.writeEndElement();
            sw.writeEndElement();
          }
          if (!StringUtils.isEmpty(mEventIQQuery.getEnd()))
          {
            sw.writeStartElement("jabber:x:data", "field");
              sw.writeAttribute("var", "end");
              sw.writeStartElement("value");
                sw.writeCharacters(mEventIQQuery.getEnd());
              sw.writeEndElement();
            sw.writeEndElement();
          }
        sw.writeEndElement();
      }
      sw.writeEndElement();
    sw.writeEndElement();
    sw.close();
  }
}
