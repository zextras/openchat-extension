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
import org.openzal.zal.Provisioning;
import org.openzal.zal.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 *
 * zmsoap -t account -m user1@example.com -p assext  ZxChatRequest/action=query_archive ../with=user2@example.com ../session_id=687c6492-9027-416c-8172-2d668154d2d1 | recode html..text
 *
 */
public class QueryArchive implements ChatOperation
{
  private final long mMax;
  private final Provisioning mProvisioning;
  private final EventManager mEventManager;
  private final SpecificAddress mSenderAddress;
  private final String mWith;
  private final String mQueryid;
  private final String mStart;
  private final String mEnd;
  private final String mNode;
  private final ArchiveInterceptorFactoryImpl mUserHistoryInterceptorFactoryImpl2;

  @Inject
  public QueryArchive(
    @Assisted("senderAddress") SpecificAddress senderAddress,
    @Assisted("queryid") String queryid,
    @Assisted("with") String with,
    @Assisted("start") String start,
    @Assisted("end") String end,
    @Assisted("node") String node,
    @Assisted("max") long max,
    Provisioning provisioning,
    ArchiveInterceptorFactoryImpl userHistoryInterceptorFactoryImpl2,
    EventManager eventManager
  )
  {
    mMax = max;
    mProvisioning = provisioning;
    mUserHistoryInterceptorFactoryImpl2 = userHistoryInterceptorFactoryImpl2;
    mEventManager = eventManager;
    mSenderAddress = senderAddress;
    mWith = with;
    mQueryid = queryid;
    mStart = start;
    mEnd = end;
    mNode = node;
  }

  @Override
  public List<Event> exec(SessionManager sessionManager, UserProvider userProvider)
    throws ChatException, ChatDbException
  {
    String user1 = mSenderAddress.withoutResource().toString();
    List<Server> allServers = mProvisioning.getAllServers();
    List<ChatAddress> addresses = new ArrayList<ChatAddress>(allServers.size());
    for (Server server : allServers)
    {
      addresses.add(new SpecificAddress(server.getName()));
    }
    String queryId = EventId.randomUUID().toString();
    mUserHistoryInterceptorFactoryImpl2.prepareQuery(queryId);
    List<Event> returnsEvents = new ArrayList<Event>();
    List<Event> historyEvents = new ArrayList<Event>();
    try
    {
      historyEvents.add(new EventIQQuery(
        EventId.randomUUID(),
        mSenderAddress,
        queryId,
        new Target(addresses),
        "",
        mWith,
        "",
        "",
        mMax
      ));

      mEventManager.dispatchUnfilteredEvents(historyEvents);

      List<EventMessageHistory> histories = mUserHistoryInterceptorFactoryImpl2.waitAndGetMessages(queryId);

      Collections.sort(histories, new Comparator<EventMessageHistory>()
      {
        @Override
        public int compare(EventMessageHistory m1, EventMessageHistory m2)
        {
          return (int) (m1.getOriginalMessage().getTimestamp() - m2.getOriginalMessage().getTimestamp());
        }
      });

      String lastMessageId = "";
      String firstMessageId = "";
      Iterator<EventMessageHistory> it = histories.iterator();
      while (it.hasNext())
      {
        EventMessageHistory message = it.next();
        lastMessageId = message.getId().toString();
        if (firstMessageId.isEmpty())
        {
          firstMessageId = lastMessageId;
        }
        returnsEvents.add(message);
      }
      returnsEvents.add(new EventMessageHistoryLast(
        EventId.randomUUID(),
        mSenderAddress,
        queryId,
        new SpecificAddress(user1),
        firstMessageId,
        lastMessageId
      ));

    }
    catch (InterruptedException e)
    {
      throw new ChatException(e.getMessage());
    }
    finally
    {
      mUserHistoryInterceptorFactoryImpl2.purgeQuery(queryId);
    }
    return returnsEvents;
  }
}
