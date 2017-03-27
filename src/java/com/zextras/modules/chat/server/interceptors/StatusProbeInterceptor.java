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

package com.zextras.modules.chat.server.interceptors;

import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.session.Session;
import com.zextras.modules.chat.server.Relationship;
import com.zextras.modules.chat.server.events.*;
import com.zextras.modules.chat.server.session.SessionManager;
import com.zextras.modules.chat.server.status.Status;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.User;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.exceptions.ChatException;
import org.openzal.zal.exceptions.ZimbraException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatusProbeInterceptor implements EventInterceptor
{
  private final UserProvider     mOpenUserProvider;
  private final SessionManager   mSessionManager;
  private final EventStatusProbe mEventStatusProbe;

  public StatusProbeInterceptor(
    UserProvider openUserProvider,
    SessionManager sessionManager,
    EventStatusProbe eventStatusProbe)
  {
    mOpenUserProvider = openUserProvider;
    mSessionManager = sessionManager;
    mEventStatusProbe = eventStatusProbe;
  }

  @Override
  public void intercept(EventManager eventManager, SpecificAddress target)
    throws ChatException, ChatDbException, ZimbraException
  {
    List<Session> senderSessionList = mSessionManager.getUserSessions(target);
    if (senderSessionList.isEmpty()) {
      return;
    }

    User user = mOpenUserProvider.getUser(target);

    if (user.hasRelationship(mEventStatusProbe.getSender()) )
    {
      Relationship relationship = user.getRelationship(mEventStatusProbe.getSender());
      if( relationship.getType().equals(Relationship.RelationshipType.ACCEPTED) )
      {
        List<Event> statusChangeEventList = createEventChangeListForSender(senderSessionList);
        eventManager.dispatchUnfilteredEvents(statusChangeEventList);
      }
    }
  }

  private List<Event> createEventChangeListForSender(List<Session> senderSessionList)
  {
    if (senderSessionList.isEmpty()) {
      return Collections.emptyList();
    }

    List<Event> eventList = new ArrayList<Event>(senderSessionList.size());
    for (Session senderSession : senderSessionList) {

      if( senderSession.getLastStatus().getType().equals(Status.StatusType.INVISIBLE) ) {
        continue;
      }

      eventList.add(
        new EventStatusChanged(
          senderSession.getMainAddress(),
          new Target(mEventStatusProbe.getSender().withoutSession()),
          senderSession.getLastStatus()
        )
      );
    }

    return eventList;
  }
}
