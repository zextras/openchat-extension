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


import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zextras.lib.log.ChatLog;
import com.zextras.lib.sql.DbPrefetchIterator;
import com.zextras.modules.chat.properties.ChatProperties;
import com.zextras.modules.chat.server.ImMessage;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.sql.ImMessageStatements;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventIQQuery;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.events.EventManager;
import com.zextras.modules.chat.server.events.EventMessage;
import com.zextras.modules.chat.server.events.EventMessageAck;
import com.zextras.modules.chat.server.events.EventMessageHistory;
import com.zextras.modules.chat.server.events.EventMessageHistoryLast;
import com.zextras.modules.chat.server.events.EventType;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import org.openzal.zal.Account;
import org.openzal.zal.Provisioning;
import org.openzal.zal.Utils;
import org.openzal.zal.exceptions.ZimbraException;
import org.openzal.zal.lib.FakeClock;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ArchiveInterceptorFactoryImpl extends StubEventInterceptorFactory implements ArchiveInterceptorFactory
{
  public interface MessageHistoryFactory
  {
    void create(EventMessageHistory message);
    void create(EventMessageHistoryLast message);
  }

  private final Provisioning   mProvisioning;
  private final ChatProperties mChatProperties;
  private final ImMessageStatements mImMessageStatements;
  private final EventManager mEventManager;
  private final Set<MessageHistoryFactory> mListeners;

  @Inject
  public ArchiveInterceptorFactoryImpl(
    Provisioning provisioning,
    ChatProperties chatProperties,
    ImMessageStatements imMessageStatements,
    EventManager eventManager
  )
  {
    mProvisioning = provisioning;
    mChatProperties = chatProperties;
    mImMessageStatements = imMessageStatements;
    mEventManager = eventManager;
    mListeners = Collections.newSetFromMap(new ConcurrentHashMap<MessageHistoryFactory,Boolean>());
  }

  public void register(MessageHistoryFactory callback)
  {
    mListeners.add(callback);
  }

  public void unRegister(MessageHistoryFactory callback)
  {
    mListeners.remove(callback);
  }

  @Override
  public EventInterceptor interpret(final EventMessage eventMessage)
  {
    return new EventInterceptor()
    {
      @Override
      public boolean intercept(EventManager eventManager, SpecificAddress target) throws ChatException, ChatDbException, ZimbraException
      {
        String sender = eventMessage.getSender().withoutResource().toString();
        if (mChatProperties.isChatHistoryEnabled(sender))
        {
            Account account = mProvisioning.assertAccountByName(target.toString());
            if (mProvisioning.onLocalServer(account))
            {
              try
              {
                ImMessage message = new ImMessage(
                  eventMessage.getId().toString(),
                  eventMessage.getSender().withoutResource().toString(),
                  target.withoutResource().toString(),
                  eventMessage.getMessage(),
                  EventType.Chat);

              mImMessageStatements.insert(message);
            }
            catch (Exception e)
            {
              ChatLog.log.warn("Cannot save history for " + sender + ": " + Utils.exceptionToString(e));
            }
            return true;
          }
        }
        return false;
      }
    };
  }

  @Override
  public EventInterceptor interpret(final EventMessageAck eventMessage)
  {
    return new EventInterceptor()
    {
      @Override
      public boolean intercept(EventManager eventManager, SpecificAddress target) throws ChatException, ChatDbException, ZimbraException
      {
        String sender = eventMessage.getSender().withoutResource().toString();
        try
        {
          Account account = mProvisioning.assertAccountByName(sender);
          if (mProvisioning.onLocalServer(account))
          {
            mImMessageStatements.upsertMessageRead(
              sender,
              eventMessage.getTarget().toSingleAddress(),
              eventMessage.getTimestamp(),
              eventMessage.getId().toString()
            );
          }
        }
        catch (Exception e)
        {
          ChatLog.log.warn("Cannot save message_read for " + target.withoutSession().toString() + ": " + Utils.exceptionToString(e));
        }
        return true;
      }
    };
  }

  @Override
  public EventInterceptor interpret(final EventIQQuery event) throws ChatException
  {
    return new EventInterceptor()
    {
      @Override
      public boolean intercept(EventManager eventManager, SpecificAddress target) throws ChatException, ChatDbException, ZimbraException
      {
        String queryId = event.getQueryId();
        String sender = event.getSender().withoutResource().toString();
        List<Event> events = query(sender, event.getWith().or(""), queryId,event.getStart(),event.getEnd(), event.getMax());
        mEventManager.dispatchUnfilteredEvents(events);
        return true;
      }
    };
  }

  @Override
  public EventInterceptor interpret(final EventMessageHistory eventMessage)
  {
    return new EventInterceptor()
    {
      @Override
      public boolean intercept(EventManager eventManager, SpecificAddress target) throws ChatException, ChatDbException, ZimbraException
      {
        for (MessageHistoryFactory listener : mListeners)
        {
          listener.create(eventMessage);
        }
        return true;
      }
    };
  }

  @Override
  public EventInterceptor interpret(final EventMessageHistoryLast eventMessage)
  {
    return new EventInterceptor()
    {
      @Override
      public boolean intercept(EventManager eventManager, SpecificAddress target) throws ChatException, ChatDbException, ZimbraException
      {
        for (MessageHistoryFactory listener : mListeners)
        {
          listener.create(eventMessage);
        }
        return true;
      }
    };
  }

  private List<Event> query(String requester, String target, String queryId,Optional<Long> start,Optional<Long> end, Optional<Integer> max) throws ChatException
  {
    List<Event> events = new ArrayList<Event>();

    try
    {
      if (max.isPresent() && max.get() == 0)
      {
        if (target.isEmpty())
        {
          Map<String, Long> lastMessageRead = mImMessageStatements.getLastMessageRead(requester);
          for (String user : lastMessageRead.keySet())
          {
            Long timestamp = lastMessageRead.get(user);
            int count = mImMessageStatements.getCountMessageToRead(requester, user, timestamp);
            events.add(new EventMessageHistoryLast(
              EventId.randomUUID(),
              new SpecificAddress(user),
              "", // TODO: add a new event?
              new SpecificAddress(requester),
              "",
              "",
              Optional.<Integer>of(count),
              timestamp
            ));
          }
        }
        else
        {
          Long timestamp = mImMessageStatements.getLastMessageRead(requester,target);
          int count = mImMessageStatements.getCountMessageToRead(requester, target, timestamp);
          events.add(new EventMessageHistoryLast(
            EventId.randomUUID(),
            new SpecificAddress(target),
            "",
            new SpecificAddress(requester),
            "",
            "",
            Optional.<Integer>of(count),
            timestamp
          ));
        }
      }
      else
      {
        String sender = mProvisioning.getLocalServer().getName();
        DbPrefetchIterator<ImMessage> it = mImMessageStatements.query(requester, target, start, end, max);
        String lastMessageId = "";
        String firstMessageId = "";
        while (it.hasNext())
        {
          ImMessage message = it.next();
          lastMessageId = message.getId();
          if (firstMessageId.isEmpty())
          {
            firstMessageId = lastMessageId;
          }
          events.add(new EventMessageHistory(
            EventId.randomUUID(),
            new SpecificAddress(sender),
            queryId,
            new SpecificAddress(requester),
            new EventMessage(
              EventId.fromString(message.getId()),
              new SpecificAddress(message.getSender()),
              new Target(new SpecificAddress(message.getDestination())),
              message.getText(),
              new FakeClock(message.getSentTimestamp())
            )
          ));
        }
        events.add(new EventMessageHistoryLast(
          EventId.randomUUID(),
          new SpecificAddress(sender),
          queryId,
          new SpecificAddress(requester),
          firstMessageId,
          lastMessageId,
          max,
          System.currentTimeMillis()
        ));
      }
    } catch (SQLException e)
    {
      throw new ChatException(Utils.exceptionToString(e));
    }

    return events;
  }
}