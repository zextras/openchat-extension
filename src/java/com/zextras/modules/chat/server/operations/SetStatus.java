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
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.status.FixedStatus;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.session.Session;
import com.zextras.modules.chat.server.session.SessionManager;
import com.zextras.modules.chat.server.session.SessionUUID;
import com.zextras.modules.chat.server.status.Status;
import com.zextras.modules.chat.server.status.StatusId;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.User;
import com.zextras.modules.chat.server.address.FriendsAddress;
import com.zextras.modules.chat.server.events.EventStatusChanged;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.exceptions.StatusDoesNotExistException;

import java.util.Arrays;
import java.util.List;

public class SetStatus implements ChatOperation
{
  private final SessionUUID    mSessionId;
  private final StatusProvider mStrategy;

  protected interface StatusProvider
  {
    Status getStatus(User user) throws StatusDoesNotExistException;
  }

  protected class LastUserStatus implements StatusProvider
  {
    @Override
    public Status getStatus(User user) throws StatusDoesNotExistException
    {
      return FixedStatus.Available;
    }
  }

  private class SetStatusFromId implements StatusProvider
  {
    private final StatusId mStatusId;

    SetStatusFromId(StatusId statusId) {mStatusId = statusId;}

    @Override
    public Status getStatus(User user) throws StatusDoesNotExistException
    {
      return FixedStatus.fromId(mStatusId);
    }
  }

  protected class SetStatusDirectly implements StatusProvider
  {
    private final Status mStatus;

    SetStatusDirectly(Status status) {mStatus = status;}

    @Override
    public Status getStatus(User user) throws StatusDoesNotExistException
    {
      return mStatus;
    }
  }

  public SetStatus(
    SessionUUID sessionId
  )
  {
    mSessionId = sessionId;
    mStrategy = new LastUserStatus();
  }

  public SetStatus(
    SessionUUID sessionId,
    StatusId statusId
  )
  {
    mStrategy = new SetStatusFromId(statusId);
    mSessionId = sessionId;
  }

  public SetStatus(
    SessionUUID sessionId,
    Status status
  )
  {
    mSessionId = sessionId;
    mStrategy = new SetStatusDirectly(status);
  }


  @Override
  public List<Event> exec(SessionManager sessionManager, UserProvider userProvider) throws ChatException, ChatDbException
  {
    Session session = sessionManager.getSessionById(mSessionId);

    Status oldStatus = session.getLastStatus();

    Status status = mStrategy.getStatus(session.getUser());

    validateStatus(status);

    ChatLog.log.info(session.getMainAddress() + " changed status to " + status.getType());

    return createEvents(
      status,
      oldStatus,
      session.getMainAddress()
    );
  }

  protected void validateStatus(Status status)
  {
    if (status.getType().equals(Status.StatusType.OFFLINE))
    {
      throw new RuntimeException("Invalid set status offline.");
    }
  }

  protected List<Event> createEvents(Status status, Status oldStatus, SpecificAddress from)
  {
    Status statusForFriends = status;

    if( status.getType().equals(Status.StatusType.INVISIBLE) )
    {
      statusForFriends = new FixedStatus(Status.StatusType.OFFLINE);
    }

    final Event statusChangedForFriends = new EventStatusChanged(
      from,
      new Target(new FriendsAddress(from)),
      statusForFriends
    );

    final Event statusChangedForMyself = new EventStatusChanged(
      from.withoutSession(),
      new Target(from.withoutSession()),
      status
    );

    return Arrays.asList( statusChangedForMyself, statusChangedForFriends );
  }
}
