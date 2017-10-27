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
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.session.SessionManager;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.User;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.EventFriendRenamed;
import com.zextras.modules.chat.server.exceptions.ChatException;
import org.openzal.zal.Provisioning;

import java.util.Collections;
import java.util.List;

/**
 * This event is invoked when a user renames one of his friends.
 * The user doing the renaming is the mSender.
 * The friend is mFriendToRename.
 * The new nickname is mNewNickname.
 * The target needs not be notified of this.
 */
public class UpsertFriend implements ChatOperation
{
  private final SpecificAddress mSender;
  private final SpecificAddress mFriendToRename;
  private final String          mNewNickname;
  private final Provisioning    mProvisioning;
  private final String          mNewGroup;

  public UpsertFriend(
    SpecificAddress sender,
    SpecificAddress friendToRename,
    String newNickname,
    String newGroup,
    Provisioning provisioning
  )
  {
    mSender = sender;
    mFriendToRename = friendToRename;
    mNewNickname = newNickname;
    mProvisioning = provisioning;
    mNewGroup = newGroup;
  }

  @Override
  public List<Event> exec(SessionManager sessionManager, UserProvider userProvider)
    throws ChatException, ChatDbException
  {
    User user = userProvider.getUser(mSender);

    if (!user.hasRelationship(mFriendToRename))
    {
      AddFriend addFriend = new AddFriend(mSender, mFriendToRename, mNewNickname, mNewGroup, mProvisioning);
      return addFriend.exec(sessionManager, userProvider);
    }
    else
    {
      Relationship relationship = user.getRelationship(mFriendToRename);

      if (!relationship.getBuddyNickname().equals(mNewNickname))
      {
        user.updateBuddyNickname(mFriendToRename, mNewNickname);
        ChatLog.log.info(mSender + " changed the nickname of " + mFriendToRename + " to " + mNewNickname);
      }

      if (!relationship.getGroup().equals(mNewGroup))
      {
        user.updateBuddyGroup(mFriendToRename, mNewGroup);
        ChatLog.log.info(mSender + " changed the group of " + mFriendToRename + " to " + mNewGroup);
      }

      Event event = new EventFriendRenamed(
        mSender.withoutSession(),
        mFriendToRename,
        mNewNickname,
        mNewGroup,
        new Target(mSender)
      );

      return Collections.singletonList(event);
    }
  }
}
