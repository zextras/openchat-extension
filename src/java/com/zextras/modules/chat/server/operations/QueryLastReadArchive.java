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

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.Target;
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
import com.zextras.modules.chat.server.interceptors.ArchiveInterceptorFactoryImpl;
import com.zextras.modules.chat.server.session.SessionManager;
import org.openzal.zal.Account;
import org.openzal.zal.Provisioning;
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
 * @see QueryArchive
 * @see ArchiveInterceptorFactoryImpl
 */
public class QueryLastReadArchive implements ChatOperation
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
  private final ArchiveInterceptorFactoryImpl mArchiveInterceptorFactory;
  private List<EventMessageHistory> mMessages;
  private Lock mLock;
  private Condition mReady;
  private String mLastMessageId;
  private String mFirstMessageId;

  @Inject
  public QueryLastReadArchive(
    @Assisted("senderAddress") SpecificAddress senderAddress,
    @Assisted("with") Optional<String> with,
    @Assisted("start") Optional<Long> start,
    @Assisted("end") Optional<Long> end,
    @Assisted("node") Optional<String> node,
    @Assisted("max") Optional<Integer> max,
    Provisioning provisioning,
    ArchiveInterceptorFactoryImpl archiveInterceptorFactory,
    EventManager eventManager
  )
  {
    mMax = max;
    mProvisioning = provisioning;
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
  }

  @Override
  public List<Event> exec(SessionManager sessionManager, UserProvider userProvider)
    throws ChatException, ChatDbException
  {
    List<Event> queryEvents = new ArrayList<Event>();
    mQueryid = EventId.randomUUID().toString();

    List<ChatAddress> addresses = new ArrayList<ChatAddress>();
    if (!mWith.isPresent())
    {
      List<Server> allServers = mProvisioning.getAllServers();
      for (Server server : allServers)
      {
        addresses.add(new SpecificAddress(server.getServerHostname()));
      }
    }
    else
    {
      Account account = mProvisioning.getAccountByName(mWith.get());
      if (account != null)
      {
        addresses.add(new SpecificAddress(account.getServerHostname()));
      }
    }
    if (!addresses.isEmpty())
    {
      queryEvents.add(new EventIQQuery(
        EventId.randomUUID(),
        mSenderAddress,
        mQueryid,
        new Target(addresses),
        mNode,
        Optional.<String>absent(),
        mStart,
        mEnd,
        mMax
      ));
    }

    return queryEvents;
  }
}
