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
import com.zextras.modules.chat.server.events.EventMessage;
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
public class QueryArchive implements ChatOperation, ArchiveInterceptorFactoryImpl.MessageHistoryFactory
{
  private final long mMax;
  private final Provisioning mProvisioning;
  private final EventManager mEventManager;
  private final SpecificAddress mSenderAddress;
  private final String mWith;
  private String mQueryid;
  private final String mStart;
  private final String mEnd;
  private final String mNode;
  private final ArchiveInterceptorFactoryImpl mArchiveInterceptorFactory;
  private List<EventMessageHistory> mMessages;

  @Inject
  public QueryArchive(
    @Assisted("senderAddress") SpecificAddress senderAddress,
    @Assisted("with") String with,
    @Assisted("start") String start,
    @Assisted("end") String end,
    @Assisted("node") String node,
    @Assisted("max") long max,
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
  }

  @Override
  public List<Event> exec(SessionManager sessionManager, UserProvider userProvider)
    throws ChatException, ChatDbException
  {
    List<Server> allServers = mProvisioning.getAllServers();
    List<ChatAddress> addresses = new ArrayList<ChatAddress>(allServers.size());
    for (Server server : allServers)
    {
      addresses.add(new SpecificAddress(server.getServerHostname()));
    }
    SpecificAddress localServer = new SpecificAddress(mProvisioning.getLocalServer().getServerHostname());

    mQueryid = EventId.randomUUID().toString();
    mArchiveInterceptorFactory.register(this);

    List<Event> returnsEvents = new ArrayList<Event>();
    List<Event> historyEvents = new ArrayList<Event>();
    try
    {
      historyEvents.add(new EventIQQuery(
        EventId.randomUUID(),
        mSenderAddress,
        mQueryid,
        new Target(localServer),
        "",
        mWith,
        "",
        "",
        mMax
      ));
      historyEvents.add(new EventIQQuery(
        EventId.randomUUID(),
        new SpecificAddress(mWith),
        mQueryid,
        new Target(addresses),
        "",
        mSenderAddress.withoutResource().toString(),
        "",
        "",
        mMax
      ));

      mEventManager.dispatchUnfilteredEvents(historyEvents);

      //List<EventMessageHistory> histories = mArchiveInterceptorFactory.waitAndGetMessages(queryId);

//      Collections.sort(histories, new Comparator<EventMessageHistory>()
//      {
//        @Override
//        public int compare(EventMessageHistory m1, EventMessageHistory m2)
//        {
//          return (int) (m1.getOriginalMessage().getTimestamp() - m2.getOriginalMessage().getTimestamp());
//        }
//      });
//
//      String lastMessageId = "";
//      String firstMessageId = "";
//      Iterator<EventMessageHistory> it = histories.iterator();
//      while (it.hasNext())
//      {
//        EventMessageHistory message = it.next();
//        lastMessageId = message.getId().toString();
//        if (firstMessageId.isEmpty())
//        {
//          firstMessageId = lastMessageId;
//        }
//        returnsEvents.add(new EventMessageHistory(
//          message.getId(),
//          new SpecificAddress(mWith),
//          mQueryid,
//          mSenderAddress,
//          message.getOriginalMessage()
//        ));
//      }
//      returnsEvents.add(new EventMessageHistoryLast(
//        EventId.randomUUID(),
//        new SpecificAddress(mWith),
//        mQueryid,
//        mSenderAddress,
//        firstMessageId,
//        lastMessageId
//      ));

    }
//    catch (InterruptedException e)
//    {
//      throw new ChatException(e.getMessage());
//    }
    finally
    {
      mArchiveInterceptorFactory.unRegister(this);
      //mArchiveInterceptorFactory.purgeQuery(queryId);
    }
    return returnsEvents;
  }

  @Override
  public void create(EventMessageHistory message)
  {
    if (mQueryid != null && !mQueryid.isEmpty() && mQueryid.equals(message.getQueryId()))
    {
      mMessages.add(message);
    }
  }

  @Override
  public void create(EventMessageHistoryLast message)
  {

  }
}
