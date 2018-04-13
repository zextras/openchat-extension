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

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.zextras.lib.Optional;
import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.AddressResolver;
import com.zextras.modules.chat.server.address.ChatAddress;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventIQQuery;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.events.EventManager;
import com.zextras.modules.chat.server.events.EventMessageHistory;
import com.zextras.modules.chat.server.events.EventMessageHistoryLast;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.interceptors.QueryArchiveInterceptorFactory;
import com.zextras.modules.chat.server.interceptors.QueryArchiveInterceptorFactoryImpl;
import com.zextras.modules.chat.server.session.SessionManager;
import org.openzal.zal.Account;
import org.openzal.zal.Provisioning;
import org.openzal.zal.ProvisioningImp;
import org.openzal.zal.Server;
import org.openzal.zal.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * zmsoap -t account -m user1@example.com -p assext  ZxChatRequest/action=query_archive ../with=user2@example.com ../session_id=687c6492-9027-416c-8172-2d668154d2d1 | recode html..text
 *
 * @see QueryArchiveInterceptorFactoryImpl
 */
public class QueryArchive implements ChatOperation, QueryArchiveInterceptorFactoryImpl.MessageHistoryFactory
{
  private final Optional<Integer> mMax;
  private final Provisioning mProvisioning;
  private final EventManager mEventManager;
  private final SpecificAddress mSenderAddress;
  private final Optional<String> mWith;
  private String mQueryid;
  private final Optional<Long> mStart;
  private final Optional<Long> mEnd;
  private final Optional<String> mNode;
  private final AddressResolver   mAddressResolver;
  private final QueryArchiveInterceptorFactory mArchiveInterceptorFactory;
  private List<EventMessageHistory> mMessages;
  private Lock mLock;
  private Condition mReady;
  private int mQueries;
  private String mLastMessageId;
  private String mFirstMessageId;

  @Inject
  public QueryArchive(
    @Assisted("senderAddress") SpecificAddress senderAddress,
    @Assisted("queryId") String queryId,
    @Assisted("with") Optional<String> with,
    @Assisted("start") Optional<Long> start,
    @Assisted("end") Optional<Long> end,
    @Assisted("node") Optional<String> node,
    @Assisted("max") Optional<Integer> max,
    Provisioning provisioning,
    AddressResolver addressResolver,
    QueryArchiveInterceptorFactory archiveInterceptorFactory,
    EventManager eventManager
  )
  {
    mMax = max;
    mProvisioning = provisioning;
    mAddressResolver = addressResolver;
    mArchiveInterceptorFactory = archiveInterceptorFactory;
    mEventManager = eventManager;
    mSenderAddress = senderAddress;
    mWith = with;
    mStart = start;
    mEnd = end;
    mNode = node;
    mMessages = new ArrayList<EventMessageHistory>();
    mLock = new ReentrantLock();
    mReady = mLock.newCondition();
    mLastMessageId = "";
    mFirstMessageId = "";
    mQueryid = queryId;
  }

  @Override
  public List<Event> exec(SessionManager sessionManager, UserProvider userProvider)
    throws ChatException, ChatDbException
  {
    SpecificAddress localServer = new SpecificAddress(mProvisioning.getLocalServer().getServerHostname());

    if (!mWith.hasValue())
    {
      throw new UnsupportedOperationException();
    }

    String hostname = mAddressResolver.tryResolveAddress(new SpecificAddress(mWith.getValue()));
    if( hostname != null )
    {
/*
       QueryArchiveInterceptorFactoryImpl will answer directly to the client because we don't need re-ordering so
       there is no need to keep track and re-order history events.
*/
      SpecificAddress hostnameAddress = new SpecificAddress(hostname);

      return Collections.<Event>singletonList(
        new EventIQQuery(
          EventId.randomUUID(),
          mSenderAddress,
          mQueryid,
          new Target(hostnameAddress),
          mNode,
          mWith,
          mStart,
          mEnd,
          mMax
        )
      );
    }

    List<Event> queryEvents = new ArrayList<Event>(2);
    Account account = mProvisioning.getAccountByName(mWith.getValue());
    if (account != null)
    {
      SpecificAddress localServerAddress = new SpecificAddress(
        mProvisioning.getLocalServer().getServerHostname()
      );

      queryEvents.add(new EventIQQuery(
        EventId.randomUUID(),
        localServerAddress,
        mQueryid,
        new Target(localServer),
        Optional.of(mSenderAddress.withoutResource().toString()),
        mWith,
        mStart,
        mEnd,
        mMax
      ));
      queryEvents.add(new EventIQQuery(
        EventId.randomUUID(),
        localServerAddress,
        mQueryid,
        new Target(new SpecificAddress(account.getServerHostname())),
        mWith,
        Optional.of(mSenderAddress.withoutResource().toString()),
        mStart,
        mEnd,
        mMax
      ));
    }
    else
    {
      return Collections.emptyList();
    }

    mQueries = queryEvents.size();
    mArchiveInterceptorFactory.register(this);
    mEventManager.dispatchUnfilteredEvents(queryEvents);

    List<Event> historyEvents = new ArrayList<Event>();
    try
    {
      mLock.lock();
      try
      {
        if (mQueries > 0)
        {
          if (!mReady.await(5000L, TimeUnit.MILLISECONDS))
          {
            ChatLog.log.warn("QueryArchive: Not all servers replied in time for address: '"+mWith+ '\'');
          }
        }
      }
      catch (InterruptedException e)
      {
        ChatLog.log.err(Utils.exceptionToString(e));
      }
      finally
      {
        mLock.unlock();
      }

      Collections.sort(mMessages, new Comparator<EventMessageHistory>()
      {
        @Override
        public int compare(EventMessageHistory m1, EventMessageHistory m2)
        {
          return Long.compare(m2.getOriginalMessage().getTimestamp(), m1.getOriginalMessage().getTimestamp());
        }
      });

      if (!mMessages.isEmpty())
      {
        mFirstMessageId = mMessages.get(0).getId().toString();
        mLastMessageId = mMessages.get(mMessages.size() - 1).getId().toString();
      }

      for (EventMessageHistory message : mMessages)
      {
        if (mMax.hasValue() && historyEvents.size() >= mMax.getValue())
        {
          break;
        }
        historyEvents.add(message);
      }
      Collections.reverse(historyEvents);
      historyEvents.add(new EventMessageHistoryLast(
        EventId.randomUUID(),
        new SpecificAddress(mWith.getValue()),
        mQueryid,
        mSenderAddress,
        mFirstMessageId,
        mLastMessageId,
        new com.zextras.lib.Optional<Integer>(historyEvents.size()),
        System.currentTimeMillis()
      ));
    }
    finally
    {
      mArchiveInterceptorFactory.unRegister(this);
    }
    return historyEvents;
  }

  @Override
  public void create(EventMessageHistory message)
  {
    mLock.lock();
    try
    {
      if (mQueryid.equals(message.getQueryId()))
      {
        mMessages.add(new EventMessageHistory(
          message.getId(),
          new SpecificAddress(mWith.getValue()),
          mQueryid,
          mSenderAddress,
          message.getOriginalMessage()
        ));
      }
    }
    finally
    {
      mLock.unlock();
    }
  }

  @Override
  public void create(EventMessageHistoryLast message)
  {
    mLock.lock();
    try
    {
      if (mQueryid.equals(message.getQueryId()))
      {
        mQueries--;
        if (mQueries <= 0)
        {
          mReady.signal();
        }
      }
    }
    finally
    {
      mLock.unlock();
    }
  }
}
