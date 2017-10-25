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

import com.zextras.lib.AccountHelper;
import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.relationship.Relationship;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.User;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.events.*;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.operations.ProbeStatus;
import org.openzal.zal.Provisioning;
import org.openzal.zal.exceptions.ZimbraException;
import com.zextras.modules.chat.server.status.FixedStatus;

import java.util.Arrays;
import java.util.Collections;


public class FriendAcceptedEventInterceptor implements EventInterceptor {
  private final UserProvider        mOpenUserProvider;
  private final EventFriendAccepted mEventFriendAccepted;
  private final Provisioning        mProvisioning;

  public FriendAcceptedEventInterceptor(
    UserProvider openUserProvider,
    EventFriendAccepted eventFriendAccepted,
    Provisioning provisioning
  )
  {
    mOpenUserProvider = openUserProvider;
    mEventFriendAccepted = eventFriendAccepted;
    mProvisioning = provisioning;
  }

  @Override
  public void intercept(EventManager eventManager, SpecificAddress target)
    throws ChatException, ChatDbException, ZimbraException
  {
    if (mEventFriendAccepted.getSender().equals(target))
    {
      return;
    }

    User user = mOpenUserProvider.getUser(target);
    SpecificAddress senderAddress = mEventFriendAccepted.getSender();

    if (user.hasRelationship(senderAddress))
    {

      Relationship relationship = user.getRelationship(senderAddress);
      if (relationship.getType().equals(Relationship.RelationshipType.INVITED) )  {
        user.updateRelationshipType(senderAddress, Relationship.RelationshipType.ACCEPTED);
      }

      eventManager.dispatchUnfilteredEvents(
        Collections.<Event>singletonList(
          new EventStatusChanged(
            target,
            new Target(senderAddress),
            FixedStatus.Offline
          )
      ));

      ProbeStatus probeStatus = new ProbeStatus(target,senderAddress);
      eventManager.execOperations(Arrays.<ChatOperation>asList(probeStatus));
      ChatLog.log.info(senderAddress + " is now friend with " + user.getAddress());
    }
    else
    {
      String nickname = new AccountHelper(
        senderAddress.toString(),
        mProvisioning
      ).getName();

      user.addRelationship(senderAddress, Relationship.RelationshipType.NEED_RESPONSE, nickname, "");
      eventManager.dispatchUnfilteredEvents(
        Collections.<Event>singletonList(
          new EventStatusChanged(
              target,
              new Target(senderAddress),
              FixedStatus.Offline
          )
      ));

      Event event = new EventFriendAddedForClient(
        mEventFriendAccepted.getSender(),
        mEventFriendAccepted.getSender(),
        nickname,
        new Target(target)
      );

      eventManager.dispatchUnfilteredEvents(Arrays.asList(event));
      ChatLog.log.info(senderAddress + " added " + user.getAddress());
    }
  }
}
