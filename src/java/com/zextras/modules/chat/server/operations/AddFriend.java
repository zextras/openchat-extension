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

import com.zextras.lib.AccountHelper;
import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.*;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventFriendAdded;
import com.zextras.modules.chat.server.events.EventFriendBackAdded;
import com.zextras.modules.chat.server.events.FriendNotFoundEvent;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.session.SessionManager;
import org.openzal.zal.Account;
import org.openzal.zal.Provisioning;
import org.openzal.zal.exceptions.ZimbraException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This event is invoked when a user first adds another user as friend.
 * The user requesting the friendship is the mSender.
 */
public class AddFriend implements ChatOperation
{
  private       SpecificAddress mFriendToAdd;
  private final SpecificAddress mSender;
  private       String          mNickname;
  private final String          mGroup;
  private final Provisioning    mProvisioning;

  public AddFriend(
    SpecificAddress sender,
    SpecificAddress friendToAdd,
    String nickname,
    String group,
    Provisioning provisioning
  )
  {
    mFriendToAdd = friendToAdd;
    mSender = sender;
    mNickname = nickname;
    mGroup = group;
    mProvisioning = provisioning;
  }

  @Override
  public List<Event> exec(SessionManager sessionManager, UserProvider userProvider)
    throws ChatException, ChatDbException
  {

    try
    {
      Account account = mProvisioning.getAccountByName(mFriendToAdd.toString());
      if (account == null || ! account.getName().equals(mFriendToAdd.toString()))
      {
        return Collections.<Event>singletonList(new FriendNotFoundEvent(mSender, mFriendToAdd));
      }
    }
    catch (ZimbraException e)
    {
      ChatException newEx = new ChatException(e.getMessage());
      newEx.initCause(e);
      throw newEx;
    }

    User user = userProvider.getUser(mSender);
    if (!user.hasRelationship(mFriendToAdd))
    {
      AccountHelper accountHelper = new AccountHelper(
        mFriendToAdd.toString(),
        mProvisioning
      );

      if (mNickname.isEmpty()) {
        mNickname = accountHelper.getName();
      }
      mFriendToAdd = new SpecificAddress(accountHelper.getMainAddress());
      user.addRelationship(mFriendToAdd, Relationship.RelationshipType.INVITED, mNickname, mGroup);
    }

    List<Event> events = new ArrayList<Event>(2);

    EventFriendAdded eventFriendAdded = new EventFriendAdded(
      mSender,
      mFriendToAdd
    );

    EventFriendBackAdded eventFriendBackAdded = new EventFriendBackAdded(
      mSender.withoutSession(),
      eventFriendAdded.getId(),
      mFriendToAdd,
      mNickname
    );

    events.add(eventFriendBackAdded);
    events.add(eventFriendAdded);

    ChatLog.log.info(mSender + " added " + mFriendToAdd);

    return events;
  }
}
