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
import com.zextras.modules.chat.server.relationship.Relationship;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventFriendBackRemove;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.status.FixedStatus;
import com.zextras.modules.chat.server.session.SessionManager;
import com.zextras.modules.chat.server.status.Status;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.User;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.EventStatusChanged;
import com.zextras.modules.chat.server.exceptions.ChatException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RemoveFriend implements ChatOperation
{
  private final SpecificAddress mSender;
  private final SpecificAddress mFriendToRemove;

  public RemoveFriend(
    SpecificAddress sender,
    SpecificAddress friendToRemove
  )
  {
    mSender = sender;
    mFriendToRemove = friendToRemove;
  }

  @Override
  public List<Event> exec(SessionManager sessionManager, UserProvider userProvider) throws ChatException, ChatDbException
  {
    User senderUser = userProvider.getUser(mSender);
    if( senderUser.hasRelationship(mFriendToRemove) )
    {
      Relationship.RelationshipType oldRelationshipType = senderUser.getRelationship(mFriendToRemove).getType();
      senderUser.removeRelationship(mFriendToRemove);

      List<Event> events = new ArrayList<Event>(2);

      if( oldRelationshipType.equals(Relationship.RelationshipType.ACCEPTED))
      {
        events.add( new EventStatusChanged(
          mSender,
          new Target(mFriendToRemove),
          new FixedStatus( Status.StatusType.OFFLINE )
        ));
      }

      EventFriendBackRemove eventFriendBackRemove = new EventFriendBackRemove(mSender.withoutSession(), mFriendToRemove);
      events.add(eventFriendBackRemove);

      ChatLog.log.info(mSender + " removed " + mFriendToRemove);

      return events;
    }
    else
    {
      return Collections.emptyList();
    }
  }
}
