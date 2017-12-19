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

package com.zextras.modules.chat.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventManager;
import com.zextras.modules.chat.server.xmpp.XmppEventFactory;
import org.openzal.zal.Utils;

import java.util.List;

@Singleton
public class LocalXmppReceiver
{
  private final EventManager mEventManager;
  private final XmppEventFactory mXmppEventFactory;

  @Inject
  public LocalXmppReceiver(
    EventManager eventManager,
    XmppEventFactory xmppEventFactory
  )
  {
    mEventManager = eventManager;
    mXmppEventFactory = xmppEventFactory;
  }

  public void processStanza(String stanza)
  {
    ChatLog.log.debug("LocalXmppReceiver: processStanza: "+stanza);
    try
    {
      List<Event> events = mXmppEventFactory.createEvents(stanza);
//      ZELog.chat.debug("LocalXmppReceiver: events: "+events.toString());
      mEventManager.dispatchUnfilteredEvents(events);
    }
    catch (XmppEventFactory.InvalidStanzaType ex)
    {
      ChatLog.log.warn("Invalid stanza (XMPP): " + stanza);
    }
    catch (Throwable ex)
    {
      ChatLog.log.warn("Exception (XMPP): " + stanza + " " + Utils.exceptionToString(ex));
    }
  }
}
