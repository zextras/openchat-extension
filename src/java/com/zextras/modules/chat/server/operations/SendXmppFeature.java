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

package com.zextras.modules.chat.server.operations;

import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.session.SessionManager;
import com.zextras.modules.chat.server.events.EventXmppSessionFeatures;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.xmpp.netty.StanzaProcessor;

import java.util.Collections;
import java.util.List;

public class SendXmppFeature implements ChatOperation
{
  private final StanzaProcessor.XmppConnectionHandler mXmppConnectionHandler;

  public SendXmppFeature(StanzaProcessor.XmppConnectionHandler xmppConnectionHandler) {
    mXmppConnectionHandler = xmppConnectionHandler;
  }

  @Override
  public List<Event> exec(SessionManager mSessionManager, UserProvider userProvider) throws ChatException, ChatDbException
  {
    if (!mXmppConnectionHandler.getSession().isProxy())
    {
      Event event = new EventXmppSessionFeatures(
        mXmppConnectionHandler.getSession().getExposedAddress(),
        mXmppConnectionHandler.getSession().isBindable(),
        mXmppConnectionHandler.getSession().getAvailableAuthentications(),
        mXmppConnectionHandler.getSession().isUsingSSL()
      );
      mXmppConnectionHandler.getSession().getEventQueue().queueEvent(event);
    }


    return Collections.emptyList();
  }
}
