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

package com.zextras.modules.chat.server.xmpp;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.xmpp.decoders.EventDecoder;
import com.zextras.modules.chat.server.xmpp.decoders.MessageAckDecoder;
import com.zextras.modules.chat.server.xmpp.decoders.MessageDecoder;
import com.zextras.modules.chat.server.xmpp.decoders.PresenceDecoder;
import com.zextras.modules.chat.server.xmpp.decoders.StatusProbeDecoder;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.openzal.zal.Provisioning;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Singleton
public class XmppEventFactoryImpl implements XmppEventFactory
{
  private final StanzaRecognizer mStanzaRecognizer;
  private final SchemaProvider   mSchemaProvider;
  private       Provisioning     mProvisioning;
  private final UserProvider     mOpenUserProvider;

  @Inject
  public XmppEventFactoryImpl(
    StanzaRecognizer stanzaRecognizer,
    SchemaProvider schemaProvider,
    Provisioning provisioning,
    UserProvider openUserProvider
  )
  {
    mStanzaRecognizer = stanzaRecognizer;
    mSchemaProvider = schemaProvider;
    mProvisioning = provisioning;
    mOpenUserProvider = openUserProvider;
  }

  public List<Event> createEvents(String stanza)
    throws XmppEventFactory.InvalidStanzaType, XMLStreamException
  {
    EventDecoder parser = createEventDecoder(stanza);
    try
    {
      ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(stanza.getBytes("UTF-8"));
      return parser.decode(byteArrayInputStream);
    }
    catch (UnsupportedEncodingException e)
    {
      throw new RuntimeException("unsupported UTF-8??");
    }
  }

  public EventDecoder createEventDecoder(String stanza) throws XmppEventFactory.InvalidStanzaType, XMLStreamException
  {
    StanzaRecognizer.StanzaType stanzaType = mStanzaRecognizer.recognize(stanza);
    switch (stanzaType)
    {
      case Presence:
        return new PresenceDecoder(mSchemaProvider, mProvisioning, mOpenUserProvider);

      case Message:
        return new MessageDecoder(mSchemaProvider);

      case MessageAck:
        return new MessageAckDecoder(mSchemaProvider);

      case StatusProbe:
        return new StatusProbeDecoder(mSchemaProvider);

      case Unknown:
      default:
        throw new InvalidStanzaType("Invalid stanza of type: " + stanzaType.toString());
    }
  }

}
