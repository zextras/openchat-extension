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
import com.zextras.modules.chat.server.*;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.events.*;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.session.SessionManager;
import org.openzal.zal.Provisioning;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AcceptFriend implements ChatOperation
{
  private final SpecificAddress mSender;
  private final SpecificAddress mFriend;
  private final Provisioning    mProvisioning;

  public AcceptFriend(
    SpecificAddress sender,
    SpecificAddress friend,
    Provisioning provisioning
  )
  {
    mSender = sender;
    mFriend = friend;
    mProvisioning = provisioning;
  }

  @Override
  public List<Event> exec(SessionManager sessionManager, UserProvider userProvider)
    throws ChatException, ChatDbException
  {
    User user = userProvider.getUser(mSender);

    if (user.hasAddressEquals(mFriend))
    {
      return Collections.emptyList();
    }

    user.updateRelationshipType(mFriend, Relationship.RelationshipType.ACCEPTED);

    EventFriendAccepted eventFriendAccepted = new EventFriendAccepted(
      mSender,
      mFriend,
      new Target(mFriend)
    );

    EventStatusProbe eventStatusProbe = new EventStatusProbe(
      eventFriendAccepted.getId(),
      mSender,
      new Target(mFriend.withoutSession())
    );

    EventFriendBackAccepted eventFriendBackAccepted = new EventFriendBackAccepted(
      eventFriendAccepted.getId(),
      mSender.withoutSession(),
      mFriend
    );

    ChatLog.log.info(mSender + " is now friend with " + mFriend);

    return Arrays.<Event>asList(
      eventFriendAccepted,
      eventFriendBackAccepted,
      eventStatusProbe
    );
  }
}
