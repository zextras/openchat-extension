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

package com.zextras.modules.chat.server.operations;

import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.FeatureNotImplementedEvent;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.session.Session;
import com.zextras.modules.chat.server.session.SessionManager;
import com.zextras.modules.chat.server.session.SessionUUID;
import com.zextras.modules.chat.server.xmpp.parsers.IQRequestType;

import java.util.Collections;
import java.util.List;

public class FeatureNotImplementedAction implements ChatOperation
{
  private final IQRequestType mRequestType;
  private final String        mRequestId;
  private final String        mReceiver;
  private final SessionUUID mSessionID;

  public FeatureNotImplementedAction(
    SessionUUID sessionID,
    String receiver,
    IQRequestType requestType,
    String requestId
  )
  {
    mSessionID = sessionID;
    mReceiver = receiver;
    mRequestType = requestType;
    mRequestId = requestId;
  }

  @Override
  public List<Event> exec(SessionManager sessionManager, UserProvider userProvider)
    throws ChatException, ChatDbException
  {
    Session session = sessionManager.getSessionById(mSessionID);
    FeatureNotImplementedEvent event = new FeatureNotImplementedEvent(
      session.getExposedAddress().resourceAddress(),
      mReceiver,
      mRequestType,
      mRequestId
    );

    if (!event.isResponseRequired())
    {
      ChatLog.log.warn("Unsupported iq stanza");
      return Collections.emptyList();
    }

    session.getEventQueue().queueEvent(
      event
    );

    return Collections.emptyList();
  }
}
