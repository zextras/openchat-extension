/*
 * Copyright (C) 2018 ZeXtras S.r.l.
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

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.zextras.lib.Optional;
import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.ImMessage;
import com.zextras.modules.chat.server.User;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.db.sql.ImMessageStatements;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventLastMessageInfo;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.relationship.Relationship;
import com.zextras.modules.chat.server.session.SessionManager;
import org.apache.commons.lang3.tuple.Pair;
import org.openzal.zal.Utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LastMessageInfoOperation implements ChatOperation
{
  private final SpecificAddress     mSenderAddress;
  private final ImMessageStatements mImMessageStatements;

  @Inject
  public LastMessageInfoOperation(
    @Assisted SpecificAddress senderAddress,
    ImMessageStatements imMessageStatements
  )
  {
    mSenderAddress = senderAddress;
    mImMessageStatements = imMessageStatements;
  }

  @Override
  public List<Event> exec(SessionManager sessionManager, UserProvider userProvider) throws ChatException
  {
    User user = userProvider.getUser(mSenderAddress);
    Collection<Relationship> relationships = user.getRelationships();
    List<Event> events = new ArrayList<Event>(relationships.size());
    for (Relationship relationship : relationships)
    {
      Optional<Pair<Long, String>> lastMessageSentInfo = Optional.sEmptyInstance;

      SpecificAddress buddy = relationship.getBuddyAddress();
      try
      {
        List<ImMessage> sentMessages = mImMessageStatements.query(
          mSenderAddress.withoutResource().toString(),
          buddy.withoutResource().toString(),
          Optional.sEmptyInstance,
          Optional.sEmptyInstance,
          Optional.of(1)
        );
        if (!sentMessages.isEmpty())
        {
          ImMessage lastMessageSent = sentMessages.get(0);
          lastMessageSentInfo = Optional.of(
            Pair.of(
              lastMessageSent.getSentTimestamp(),
              lastMessageSent.getId()
            )
          );
        }

        events.add(
          new EventLastMessageInfo(
            mSenderAddress,
            buddy,
            Optional.of(buddy),
            lastMessageSentInfo
          )
        );
      }
      catch (SQLException e)
      {
        ChatLog.log.err(Utils.exceptionToString(e));
      }
    }
    return events;
  }
}
