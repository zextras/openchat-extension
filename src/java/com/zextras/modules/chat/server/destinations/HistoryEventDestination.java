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
import com.zextras.modules.chat.server.history.ImHistoryQueue;
import com.zextras.modules.chat.server.Priority;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.*;
import com.zextras.modules.chat.server.interceptors.EventInterceptor;
import com.zextras.modules.chat.server.interceptors.QueryArchiveInterceptorFactory;
import org.openzal.zal.Utils;

import java.util.*;


@Singleton
public class HistoryEventDestination implements EventDestination, EventDestinationProvider, Service
{
  private final EventRouter                   mEventRouter;
  private final EventManager                  mEventManager;
  private final QueryArchiveInterceptorFactory mUserHistoryInterceptorFactory;
  private final ImHistoryQueue                mImHistoryQueue;
  private final Priority mPriority = new Priority(4);

  @Inject
  public HistoryEventDestination(
    EventRouter eventRouter,
    EventManager eventManager,
    QueryArchiveInterceptorFactory userHistoryInterceptorFactory,
    ImHistoryQueue imHistoryQueue
  )
  {
    mEventRouter = eventRouter;
    mEventManager = eventManager;
    mUserHistoryInterceptorFactory = userHistoryInterceptorFactory;
    mImHistoryQueue = imHistoryQueue;
  }

  @Override
  public boolean deliverEvent(Event event, SpecificAddress target)
  {
    try
    {
      EventInterceptor interceptor = event.interpret(mUserHistoryInterceptorFactory);
      return interceptor.intercept(mEventManager,target);
    }
    catch (Exception ex)
    {
      ChatLog.log.warn("unable to relay message: " + Utils.exceptionToString(ex));
      ChatLog.log.debug("event: " + event.getClass().getName());
      return true;
    }
  }

  @Override
  public Collection<? extends EventDestination> getDestinations(SpecificAddress address)
  {
    return Arrays.asList(this);
  }

  @Override
  public Priority getPriority() {
    return mPriority;
  }

  @Override
  public void start() throws ServiceStartException
  {
    mEventRouter.plugDestinationProvider(this);
    mImHistoryQueue.start();
  }

  @Override
  public void stop()
  {
    mEventRouter.unplugDestinationProvider(this);
    mImHistoryQueue.stop();
  }
}
