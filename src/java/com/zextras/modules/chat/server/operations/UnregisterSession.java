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

import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.exceptions.NoSuchSessionException;
import com.zextras.modules.chat.server.session.SessionManager;
import com.zextras.modules.chat.server.session.SessionUUID;
import com.zextras.modules.chat.server.status.FixedStatus;

import java.util.Collections;
import java.util.List;

public class UnregisterSession implements ChatOperation
{
  private final SessionUUID mSessionId;
  private final SpecificAddress mSender;

  public UnregisterSession(
    SpecificAddress sender,
    SessionUUID sessionId
  )
  {
    mSessionId = sessionId;
    mSender = sender;
  }

  @Override
  public List<Event> exec(SessionManager sessionManager, UserProvider userProvider) throws ChatException, ChatDbException
  {
    List<Event> events = Collections.emptyList();
    try
    {
      events = new SetStatusOnLoginOrLogout(
        mSessionId, FixedStatus.Offline
      ).exec(sessionManager, userProvider);
    }
    catch (NoSuchSessionException ex)
    {
      ChatLog.log.debug("session "+mSessionId+" not found while terminating it");
    }

    sessionManager.terminateSessionById(mSessionId);
    return events;
  }
}
