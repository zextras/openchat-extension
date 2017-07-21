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

import com.zextras.modules.chat.server.Relationship;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.User;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventFriendRenamed;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.session.SessionManager;

import java.util.ArrayList;
import java.util.List;


public class RenameGroup implements ChatOperation
{
  private final SpecificAddress mTargetAddress;
  private final String          mCurrentGroupName;
  private final String          mNewGroupName;

  public RenameGroup(SpecificAddress targetAddress,
                     String currentGroupName,
                     String newGroupName
  )
  {
    mTargetAddress = targetAddress;
    mCurrentGroupName = currentGroupName;
    mNewGroupName = newGroupName;
  }

  @Override
  public List<Event> exec(SessionManager sessionManager, UserProvider userProvider)
    throws ChatException, ChatDbException
  {
    List<Event> eventList = new ArrayList<Event>(16);

    User user = userProvider.getUser(mTargetAddress);

    for (Relationship relationship : user.getRelationships())
    {
      if (relationship.getGroup().equals(mCurrentGroupName))
      {
        user.updateBuddyGroup(
          relationship.getBuddyAddress(),
          mNewGroupName
        );

        eventList.add(
          new EventFriendRenamed(
            mTargetAddress.withoutSession(),
            relationship.getBuddyAddress(),
            relationship.getBuddyNickname(),
            mNewGroupName,
            new Target(mTargetAddress)
          )
        );
      }
    }

    return eventList;
  }
}
