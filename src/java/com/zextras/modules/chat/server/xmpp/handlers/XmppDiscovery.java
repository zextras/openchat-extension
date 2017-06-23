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

package com.zextras.modules.chat.server.xmpp.handlers;

import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.session.SessionManager;
import com.zextras.modules.chat.server.events.EventXmppDiscovery;
import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.xmpp.XmppSession;

import java.util.Collections;
import java.util.List;

public class XmppDiscovery implements ChatOperation
{
  private final XmppSession mSession;
  private final EventId     mEventId;
  private final String      mTarget;

  public XmppDiscovery(XmppSession session, EventId eventId, String target)
  {
    mSession = session;
    mEventId = eventId;
    mTarget = target;
  }

  @Override
  public List<Event> exec(SessionManager sessionManager, UserProvider userProvider) throws ChatException, ChatDbException
  {
    boolean isDomainQuery = mTarget.equals(mSession.getDomain());
    mSession.getEventQueue().queueEvent(new EventXmppDiscovery(mSession.getDomain(), mEventId, isDomainQuery));

    return Collections.emptyList();
  }
}
