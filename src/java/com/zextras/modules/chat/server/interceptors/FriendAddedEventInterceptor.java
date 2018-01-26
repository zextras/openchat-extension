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

package com.zextras.modules.chat.server.interceptors;

import com.zextras.lib.AccountHelper;
import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.relationship.Relationship;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.User;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventFriendAdded;
import com.zextras.modules.chat.server.events.EventFriendAddedForClient;
import com.zextras.modules.chat.server.events.EventManager;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.operations.AcceptFriend;
import com.zextras.modules.chat.server.operations.ChatOperation;
import org.openzal.zal.Provisioning;
import org.openzal.zal.exceptions.ZimbraException;

import java.util.Arrays;


public class FriendAddedEventInterceptor implements EventInterceptor {
  private final UserProvider     mOpenUserProvider;
  private final EventFriendAdded mEventFriendAdded;
  private final Provisioning     mProvisioning;

  public FriendAddedEventInterceptor(UserProvider openUserProvider,
                                     EventFriendAdded eventFriendAdded,
                                     Provisioning provisioning
  )
  {
    mOpenUserProvider = openUserProvider;
    mEventFriendAdded = eventFriendAdded;
    mProvisioning = provisioning;
  }

  @Override
  public boolean intercept(EventManager eventManager, SpecificAddress target)
    throws ChatException, ChatDbException, ZimbraException
  {
    if (mEventFriendAdded.getSender().equals(target))
    {
      return false;
    }

    User targetUser = mOpenUserProvider.getUser(target);
    SpecificAddress senderAddress = mEventFriendAdded.getSender();

    if (!targetUser.hasRelationship(senderAddress))
    {
      AccountHelper accountHelper = new AccountHelper(
        senderAddress.toString(),
        mProvisioning
      );

      String userNickname = accountHelper.getName();
      targetUser.addRelationship(
        senderAddress,
        Relationship.RelationshipType.NEED_RESPONSE,
        userNickname,
        ""
      );

      EventFriendAddedForClient eventFriendAddedForClient = new EventFriendAddedForClient(
        mEventFriendAdded.getSender(),
        mEventFriendAdded.getSender(),
        userNickname,
        new Target(target)
      );

      eventManager.dispatchUnfilteredEvents(
        Arrays.<Event>asList(eventFriendAddedForClient)
      );
      ChatLog.log.info(mEventFriendAdded.getSender() + " added " + targetUser.getAddress());
    }
    else
    {
      Relationship relationshipWithSender = targetUser.getRelationship(senderAddress);

      if( relationshipWithSender.isType(Relationship.RelationshipType.ACCEPTED) ||
          relationshipWithSender.isType(Relationship.RelationshipType.INVITED)   )
      {
        ChatOperation addFriend = new AcceptFriend(
          target,
          mEventFriendAdded.getSender().withoutSession(),
          mProvisioning
        );
        eventManager.execOperations(Arrays.<ChatOperation>asList(addFriend));
        ChatLog.log.info(mEventFriendAdded.getSender() + " is now friend with " + targetUser.getAddress());
      }
    }
    return true;
  }
}
