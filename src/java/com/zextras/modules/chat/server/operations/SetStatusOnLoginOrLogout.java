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

import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.FriendsAddress;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventStatusChanged;
import com.zextras.modules.chat.server.session.SessionUUID;
import com.zextras.modules.chat.server.status.FixedStatus;
import com.zextras.modules.chat.server.status.Status;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*
 * this manages only changes from not invisible to online and from invisible to not connected
 */
public class SetStatusOnLoginOrLogout extends SetStatus
{
  public SetStatusOnLoginOrLogout(SessionUUID sessionId)
  {
    super(sessionId);
  }

  public SetStatusOnLoginOrLogout(SessionUUID sessionId, Status status)
  {
    super(sessionId, status);
  }

  @Override
  protected List<Event> createEvents(Status status, Status oldStatus, SpecificAddress from)
  {
    final Event statusChangedForMyself = new EventStatusChanged(
      from.withoutSession(),
      new Target(from.withoutSession()),
      status
    );

    List<Event> events = Collections.singletonList(statusChangedForMyself);
    if (!oldStatus.isInvisible() && !status.isInvisible())
    {
        events = Arrays.asList(statusChangedForMyself, createEventsForFriends(status, from));
    }

    return events;
  }

  private Event createEventsForFriends(Status status, SpecificAddress from)
  {
    Status statusForFriends = status;
    if (status.getType().equals(Status.StatusType.INVISIBLE))
    {
      statusForFriends = new FixedStatus(Status.StatusType.OFFLINE);
    }

    return new EventStatusChanged(
      from,
      new Target(new FriendsAddress(from)),
      statusForFriends
    );
  }

  @Override
  protected void validateStatus(Status status) {}
}
