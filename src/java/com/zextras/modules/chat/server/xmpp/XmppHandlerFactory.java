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

import com.zextras.modules.chat.server.xmpp.netty.StanzaProcessor;

import javax.xml.stream.XMLStreamException;

public interface XmppHandlerFactory
{
  class UnkownStanza extends Exception
  {
    private final String mStanza;

    public UnkownStanza(String stanza) {
      super("unkonwn stanza: "+stanza);
      mStanza = stanza;
    }

    public String getStanza()
    {
      return mStanza;
    }
  }

  StanzaHandler createHandler(
    StanzaProcessor.XmppConnectionHandler connectionStatus,
    String xml
  ) throws UnkownStanza, XMLStreamException;
}
