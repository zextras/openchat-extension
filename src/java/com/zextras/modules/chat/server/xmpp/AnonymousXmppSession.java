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

import com.zextras.modules.chat.properties.ChatProperties;
import org.openzal.zal.lib.Filter;
import com.zextras.lib.filters.FilterPassAll;
import com.zextras.modules.chat.server.events.EventQueue;
import com.zextras.modules.chat.server.session.SessionUUID;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.Event;

import java.util.Collection;
import java.util.Collections;

public class AnonymousXmppSession extends XmppSession
{
  private final ChatProperties mChatProperties;

  public AnonymousXmppSession(
    SessionUUID id,
    EventQueue eventQueue,
    ChatProperties chatProperties,
    XmppEventFilter xmppEventFilter,
    XmppFilterOut xmppFilterOut
  )
  {
    super(
      id,
      eventQueue,
      new AnonymousUser(),
      new SpecificAddress("anonymous@unknown.com"),
      xmppEventFilter,
      xmppFilterOut
    );
    mChatProperties = chatProperties;
  }

  public Collection<XmppAuthentication> getAvailableAuthentications()
  {
    if( isUsingSSL() || mChatProperties.allowUnencryptedPassword() ) {
      return Collections.singletonList(new XmppAuthentication("PLAIN"));
    }
    return Collections.emptyList();
  }

  @Override
  public boolean isBindable() {
    return false;
  }

  @Override
  public Filter<Event> getOutFilter()
  {
    return new FilterPassAll<Event>();
  }
}
