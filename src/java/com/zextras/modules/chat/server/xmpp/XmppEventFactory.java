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

import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.xmpp.decoders.EventDecoder;

import javax.xml.stream.XMLStreamException;
import java.util.List;

public interface XmppEventFactory
{
  class InvalidStanzaType extends Exception
  {
    public InvalidStanzaType(String s)
    {
      super(s);
    }
  }

  List<Event> createEvents(String stanza) throws InvalidStanzaType, XMLStreamException;
  EventDecoder createEventDecoder(String stanza) throws InvalidStanzaType, XMLStreamException;
}
