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

package com.zextras.modules.chat.server.dispatch;

import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.events.EventRouter;
import com.zextras.modules.chat.server.Relationship;
import com.zextras.modules.chat.server.User;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;

public class FriendsDispatcher implements Dispatcher
{
  private final EventRouter     mEventRouter;
  private final SpecificAddress mOrigin;
  private final UserProvider    mOpenUserProvider;

  public FriendsDispatcher(SpecificAddress origin, EventRouter eventRouter, UserProvider openUserProvider)
  {
    mEventRouter = eventRouter;
    mOrigin = origin;
    mOpenUserProvider = openUserProvider;
  }

  @Override
  public void dispatch(Event event) throws ChatException, ChatDbException
  {
    final User user = mOpenUserProvider.getUser(mOrigin);

    for (Relationship relationship : user.getRelationships())
    {
      if( !shouldSendTo(relationship) ){
        continue;
      }
      mEventRouter.deliverEvent(relationship.getBuddyAddress(),event);
    }
  }

  private boolean shouldSendTo(Relationship relationship)
  {
    switch( relationship.getType() )
    {
      case ACCEPTED:
        return true;

      case INVITED:
      case NEED_RESPONSE:
      case BLOCKED:
      case UNKNOWN:
      default:
        return false;
    }
  }
}
