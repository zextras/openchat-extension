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

import com.zextras.lib.Optional;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.ChatAddress;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventIQQuery;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.interceptors.QueryArchiveInterceptorFactoryImpl;
import com.zextras.modules.chat.server.session.Session;
import com.zextras.modules.chat.server.session.SessionManager;
import com.zextras.modules.chat.server.session.SessionUUID;
import org.openzal.zal.Provisioning;
import org.openzal.zal.ProvisioningImp;
import org.openzal.zal.Server;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @see QueryArchive
 * @see QueryArchiveInterceptorFactoryImpl
 */
public class QueryLastReadArchive implements ChatOperation
{
  private final SessionUUID mSessionUUID;
  private final Optional<Integer> mMax;
  private final Provisioning mProvisioning;
  private final SessionManager mSessionManager;
  private final Optional<String> mWith;
  private String mQueryid;
  private final Optional<Long> mStart;
  private final Optional<Long> mEnd;
  private final Optional<String> mNode;
  private SpecificAddress mSenderAddress;

  @Inject
  public QueryLastReadArchive(
    @Assisted("sessionUUID") SessionUUID sessionUUID,
    @Assisted("with") Optional<String> with,
    @Assisted("start") Optional<Long> start,
    @Assisted("end") Optional<Long> end,
    @Assisted("node") Optional<String> node,
    @Assisted("max") Optional<Integer> max,
    Provisioning provisioning,
    SessionManager sessionManager
  )
  {
    mSessionUUID = sessionUUID;
    mMax = max;
    mProvisioning = provisioning;
    mSessionManager = sessionManager;
    mWith = with;
    mStart = start;
    mEnd = end;
    mNode = node;
  }

  @Override
  public List<Event> exec(SessionManager sessionManager, UserProvider userProvider)
    throws ChatException, ChatDbException
  {
    List<Event> queryEvents = new ArrayList<Event>();
    mQueryid = EventId.randomUUID().toString();

    List<ChatAddress> addresses = new ArrayList<ChatAddress>();
    List<Server> allServers = mProvisioning.getAllServers(ProvisioningImp.SERVICE_MAILBOX);
    for (Server server : allServers)
    {
      addresses.add(new SpecificAddress(server.getServerHostname()));
    }
    Session session = mSessionManager.getSessionById(mSessionUUID);
    mSenderAddress = session.getMainAddress();

    queryEvents.add(new EventIQQuery(
      EventId.randomUUID(),
      mSenderAddress,
      mQueryid,
      new Target(addresses),
      mNode,
      Optional.of(session.getMainAddress().withoutResource().toString()),
      mStart,
      mEnd,
      mMax
    ));

    return queryEvents;
  }
}
