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
import com.zextras.modules.chat.server.events.EventMessageHistory;
import com.zextras.modules.chat.server.events.EventMessageHistoryLast;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import org.openzal.zal.Account;
import org.openzal.zal.Provisioning;
import org.openzal.zal.exceptions.ZimbraException;
import org.openzal.zal.lib.FakeClock;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Singleton
public class UserHistoryInterceptorFactoryImpl2 extends StubEventInterceptorFactory implements UserHistoryInterceptorFactory
{
  private final Provisioning   mProvisioning;
  private final ChatProperties mChatProperties;
  private final ImMessageStatements mImMessageStatements;
  private final EventManager mEventManager;
  private final Map<String,MessageHistory> mMessages;

  @Inject
  public UserHistoryInterceptorFactoryImpl2(
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
    mMessages = new ConcurrentHashMap<String,MessageHistory>();
  }

  @Override
  public EventInterceptor interpret(final EventMessage eventMessage)
  {
    return new EventInterceptor()
    {
      @Override
      public void intercept(EventManager eventManager, SpecificAddress target) throws ChatException, ChatDbException, ZimbraException
      {
        if (mChatProperties.isChatHistoryEnabled(target.toString()))
        {
          try
          {
            Account account = mProvisioning.assertAccountByName(target.toString());

            ImMessage message = new ImMessage(
              eventMessage.getId().toString(),
              eventMessage.getSender().withoutResource().toString(),
              target.withoutResource().toString(),
              eventMessage.getMessage());

            if (mProvisioning.onLocalServer(account))
            {
              mImMessageStatements.insert(message);
            }
          }
          catch (Exception e)
          {
            ChatLog.log.warn("Cannot save history for " + target.withoutSession().toString() + ": " + e.getMessage());
          }
        }
      }
    };
  }

  @Override
  public EventInterceptor interpret(final EventIQQuery event) throws ChatException
  {
    return new EventInterceptor()
    {
      @Override
      public void intercept(EventManager eventManager, SpecificAddress target) throws ChatException, ChatDbException, ZimbraException
      {
        String queryId = event.getQueryId();
        if (queryId.isEmpty())
        {
          queryId = UUID.randomUUID().toString();
        }

        String sender = event.getSender().withoutResource().toString();
        Account account = mProvisioning.getAccountByName(sender);
        if (account != null && account.isLocalAccount())
        {
          prepareQuery(queryId);
        }

        MessageHistory histories = mMessages.get(queryId);
        if (histories != null)
        {
          histories.query();
        }
        List<Event> events = query(event.getSender().withoutResource().toString(), event.getWith(), queryId);
        if (!events.isEmpty())
        {
          mEventManager.dispatchUnfilteredEvents(events);
        }
      }
    };
  }

  @Override
  public EventInterceptor interpret(final EventMessageHistory eventMessage)
  {
    return new EventInterceptor()
    {
      @Override
      public void intercept(EventManager eventManager, SpecificAddress target) throws ChatException, ChatDbException, ZimbraException
      {
        MessageHistory histories = mMessages.get(eventMessage.getQueryId());
        if (histories != null)
        {
          histories.add(eventMessage);
        }
      }
    };
  }

  @Override
  public EventInterceptor interpret(final EventMessageHistoryLast eventMessage)
  {
    return new EventInterceptor()
    {
      @Override
      public void intercept(EventManager eventManager, SpecificAddress target) throws ChatException, ChatDbException, ZimbraException
      {
        MessageHistory histories = mMessages.get(eventMessage.getQueryId());
        if (histories != null)
        {
          histories.add(eventMessage);
        }
      }
    };
  }


  public MessageHistory prepareQuery(String queryId)
  {
    mMessages.remove(queryId);
    MessageHistory histories = new MessageHistory();
    histories.query();
    mMessages.put(queryId,histories);
    return histories;
  }

  public void purgeQuery(String queryId)
  {
    mMessages.remove(queryId);
  }

  public List<EventMessageHistory> waitAndGetMessages(String queryId) throws InterruptedException
  {
    try
    {
      MessageHistory histories = mMessages.get(queryId);
      if (histories != null)
      {
        List<EventMessageHistory> historyList = histories.waitAndGetUntilReady(5000L);
        return historyList;
      }
      return Collections.EMPTY_LIST;
    }
    finally
    {
      mMessages.remove(queryId);
    }
  }

  private List<Event> query(String user1,String user2,String queryId) throws ChatException
  {
    List<Event> events = new ArrayList<Event>();

    try
    {
      DbPrefetchIterator<ImMessage> it;
      if (user2.isEmpty())
      {
        it = mImMessageStatements.query(user1);
      }
      else
      {
        it = mImMessageStatements.query(user1, user2);
      }

      String lastMessageId = "";
      String firstMessageId = "";
      String sender = mProvisioning.getLocalServer().getName();
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
          new SpecificAddress(user1),
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
        new SpecificAddress(user1),
        firstMessageId,
        lastMessageId
      ));
    } catch (SQLException e)
    {
      throw new ChatException(e.getMessage());
    }

    return events;
  }

  private class MessageHistory
  {
    private List<EventMessageHistory> mList = new ArrayList<EventMessageHistory>();
    private Lock mLock = new ReentrantLock();
    private Condition mReady = mLock.newCondition();
    private int queries = 0;

    public void query()
    {
      mLock.lock();
      try
      {
        queries++;
      }
      finally
      {
        mLock.unlock();
      }
    }
    public void add(EventMessageHistory eventMessage)
    {
      mLock.lock();
      try
      {
        mList.add(eventMessage);
      }
      finally
      {
        mLock.unlock();
      }
    }

    public void add(EventMessageHistoryLast eventMessage)
    {
      mLock.lock();
      try
      {
        queries--;
        mReady.signal();
      }
      finally
      {
        mLock.unlock();
      }
    }

    public List<EventMessageHistory> waitAndGetUntilReady(long msTimeOut) throws InterruptedException
    {
      mLock.lock();
      try
      {
        if (queries > 0)
        {
          mReady.await(msTimeOut, TimeUnit.MILLISECONDS);
        }
        return new ArrayList<EventMessageHistory>(mList);
      }
      finally
      {
        mLock.unlock();
      }
    }
  }

}