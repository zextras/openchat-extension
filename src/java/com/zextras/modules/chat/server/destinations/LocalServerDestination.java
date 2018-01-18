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

package com.zextras.modules.chat.server.destinations;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zextras.lib.log.ChatLog;
import com.zextras.lib.switches.Service;
import com.zextras.modules.chat.server.DestinationQueue;
import com.zextras.modules.chat.server.Priority;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.dispatch.RoomServerHostSetProvider;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventDestination;
import com.zextras.modules.chat.server.events.EventDestinationProvider;
import com.zextras.modules.chat.server.events.EventRouter;
import com.zextras.modules.chat.server.room.PassiveRoomResolverDestination;
import org.openzal.zal.Domain;
import org.openzal.zal.Provisioning;
import org.openzal.zal.Utils;
import org.openzal.zal.Account;

import java.util.*;

@Singleton
public class LocalServerDestination implements EventDestination, EventDestinationProvider, Service
{
  public static final int DEFAULT_LOCAL_XMPP_PORT = 5269;

  private final Provisioning                   mProvisioning;
  private final Map<String, DestinationQueue>  mDestinationQueues;
  private final DestinationQueueFactory        mDestinationQueueFactory;
  private final PassiveRoomResolverDestination mPassiveRoomResolverDestination;
  private final RoomServerHostSetProvider      mRoomServerHostSetProvider;
  private final EventRouter                    mEventRouter;
  private final Priority mPriority = new Priority(2);

  @Inject
  public LocalServerDestination(
    Provisioning provisioning,
    EventRouter eventRouter,
    DestinationQueueFactory destinationQueueFactory,
    PassiveRoomResolverDestination passiveRoomResolverDestination,
    RoomServerHostSetProvider roomServerHostSetProvider
  )
  {
    mProvisioning = provisioning;
    mDestinationQueueFactory = destinationQueueFactory;
    mPassiveRoomResolverDestination = passiveRoomResolverDestination;
    mRoomServerHostSetProvider = roomServerHostSetProvider;
    mDestinationQueues = new HashMap<String, DestinationQueue>();
    mEventRouter = eventRouter;
  }

  @Override
  public void deliverEvent(Event event, SpecificAddress address)
  {
    ChatLog.log.debug("LocalServerDestination: deliverEvent: "+event.getClass().getName()+" to "+address.resourceAddress());

    try
    {
      String host;

      boolean isServerAddress = address.getDomain().isEmpty();
      if( isServerAddress )
      {
        if( mRoomServerHostSetProvider.isValidServer(address) )
        {
          host = address.toString();
        }
        else
        {
          host = null;
        }
      }
      else
      {
        Account account = mProvisioning.getAccountByName(address.toString());
        if (account == null)
        {
          host = mPassiveRoomResolverDestination.tryResolveRoomAddress(address);
        }
        else
        {
          host = account.getMailHost();
        }
      }

      if( host == null ) {
        return;
      }

      DestinationQueue destinationQueue;
      if (mDestinationQueues.containsKey(host))
      {
        destinationQueue = mDestinationQueues.get(host);
      }
      else
      {
        destinationQueue = mDestinationQueueFactory.createQueue(host);
        destinationQueue.start();
        mDestinationQueues.put(host, destinationQueue);
      }
      destinationQueue.addEvent(event, address);
    }
    catch (Exception ex)
    {
      ChatLog.log.warn("unable to relay message: " + Utils.exceptionToString(ex));
      ChatLog.log.debug("event: " + event.getClass().getName());
      ChatLog.log.debug(Utils.exceptionToString(ex));
    }
  }

  @Override
  public boolean canHandle(SpecificAddress address)
  {
    return true;
  }

  @Override
  public Collection<? extends EventDestination> getDestinations(SpecificAddress address)
  {
    return Collections.singletonList(this);
  }

  @Override
  public Priority getPriority() {
    return mPriority;
  }

  public void stop() {
    mEventRouter.unplugDestinationProvider(this);
    Set<String> queues = mDestinationQueues.keySet();
    for (String queue : queues) {
      try {
        mDestinationQueues.get(queue).stop();
      } catch (InterruptedException e) {
        ChatLog.log.err(e.getMessage());
      }
    }
  }

  public void start()
  {
    mEventRouter.plugDestinationProvider(this);
    Set<String> queues = mDestinationQueues.keySet();
    for (String queue : queues) {
      mDestinationQueues.get(queue).start();
    }
  }
}
