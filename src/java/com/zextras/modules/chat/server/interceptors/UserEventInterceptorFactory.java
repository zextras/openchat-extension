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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zextras.lib.Optional;
import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.ImMessage;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.db.sql.ImMessageStatements;
import com.zextras.modules.chat.server.events.*;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.session.Session;
import com.zextras.modules.chat.server.session.SessionManager;
import org.apache.commons.lang3.tuple.Pair;
import org.openzal.zal.Provisioning;
import org.openzal.zal.Utils;
import org.openzal.zal.exceptions.ZimbraException;

import java.sql.SQLException;
import java.util.List;


@Singleton
public class UserEventInterceptorFactory extends StubEventInterceptorFactory
{
  private       UserProvider   mOpenUserProvider;
  private final SessionManager mSessionManager;
  private final Provisioning   mProvisioning;
  private final ImMessageStatements mImMessageStatements;

  @Inject
  public UserEventInterceptorFactory(
    UserProvider openUserProvider,
    SessionManager sessionManager,
    Provisioning provisioning,
    ImMessageStatements imMessageStatements
  )
  {
    mOpenUserProvider = openUserProvider;
    mSessionManager = sessionManager;
    mProvisioning = provisioning;
    mImMessageStatements = imMessageStatements;
  }

  @Override
  public EventInterceptor interpret(EventFriendAccepted eventFriendAccepted)
  {
    return new FriendAcceptedEventInterceptor(mOpenUserProvider, eventFriendAccepted, mProvisioning);
  }

  @Override
  public EventInterceptor interpret(EventFriendAdded eventFriendAdded)
  {
    return new FriendAddedEventInterceptor(mOpenUserProvider, eventFriendAdded, mProvisioning);
  }

  @Override
  public EventInterceptor interpret(EventStatusProbe eventStatusProbe)
  {
    return new StatusProbeInterceptor(mOpenUserProvider, mSessionManager, eventStatusProbe);
  }

  @Override
  public EventInterceptor interpret(final EventLastMessageInfo event)
  {
    return new EventInterceptor()
    {
      @Override
      public boolean intercept(EventManager eventManager, SpecificAddress target) throws ChatException, ChatDbException, ZimbraException
      {
        if (event.getFinalDestination().hasValue() && event.getBuddyAddress().hasValue())
        {
          Optional<Pair<Long, String>> lastIncomingMessageInfo = Optional.sEmptyInstance;
          SpecificAddress buddyAddress = event.getBuddyAddress().get();

          try
          {
            Pair<Long, String> lastMessageRead = mImMessageStatements.getLastMessageRead(
              buddyAddress.withoutResource().toString(),
              event.getFinalDestination().get().withoutResource().toString()
            );
            long timestamp = lastMessageRead.getLeft();
            Optional<Integer> unreadCount = Optional.of(mImMessageStatements.getCountMessageToRead(
              buddyAddress.withoutResource().toString(),
              event.getFinalDestination().get().withoutResource().toString(),
              timestamp
            ));
            List<ImMessage> messages = mImMessageStatements.query(
              buddyAddress.withoutResource().toString(),
              event.getFinalDestination().get().withoutResource().toString(),
              Optional.sEmptyInstance,
              Optional.sEmptyInstance,
              Optional.of(1)
            );
            if (!messages.isEmpty())
            {
              ImMessage lastMessage = messages.get(0);
              lastIncomingMessageInfo = Optional.of(
                Pair.of(
                  lastMessage.getSentTimestamp(),
                  lastMessage.getId()
                )
              );
            }

            EventLastMessageInfo lastMessageInfo = new EventLastMessageInfo(
              buddyAddress,
              new Target(event.getFinalDestination().get()),
              Optional.sEmptyInstance,
              event.getLastSentMessageInfo(),
              lastIncomingMessageInfo,
              unreadCount,
              Optional.sEmptyInstance
            );
            eventManager.dispatchUnfilteredEvent(
              lastMessageInfo
            );
          }
          catch (SQLException e)
          {
            ChatLog.log.err(Utils.exceptionToString(e));
            throw new ChatException(e.getMessage());
          }

        }
        return false;
      }
    };
  }


  @Override
  public EventInterceptor interpret(final EventFloodControl event)
  {
    return new EventInterceptor()
    {
      @Override
      public boolean intercept(EventManager eventManager, SpecificAddress target) throws ChatException, ChatDbException, ZimbraException
      {
        List<Session> sessions = mSessionManager.getUserSessions(
          new SpecificAddress(event.getTarget().toSingleAddressIncludeResource())
        );

        for (Session session : sessions)
        {
          if (event.isFloodDetected())
          {
            session.refuseInputEvents();
          }
          else
          {
            session.acceptInputEvents();
          }
        }

        return true;
      }
    };
  }
}
