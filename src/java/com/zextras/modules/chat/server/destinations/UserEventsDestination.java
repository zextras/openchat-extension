/*
 * ZAL - The abstraction layer for Zimbra.
 * Copyright (C) 2017 ZeXtras S.r.l.
 *
 * This file is part of ZAL.
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
 * You should have received a copy of the GNU General Public License
 * along with ZAL. If not, see <http://www.gnu.org/licenses/>.
 */

package com.zextras.modules.chat.server.destinations;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zextras.lib.log.ChatLog;
import com.zextras.lib.switches.Service;
import com.zextras.modules.chat.server.Priority;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.*;
import com.zextras.modules.chat.server.interceptors.EventInterceptor;
import com.zextras.modules.chat.server.interceptors.UserEventInterceptorFactory;
import org.openzal.zal.Account;
import org.openzal.zal.Provisioning;
import org.openzal.zal.Utils;

import java.util.Arrays;
import java.util.Collection;

@Singleton
public class UserEventsDestination implements EventDestination, EventDestinationProvider, Service
{
  private final Provisioning                mProvisioning;
  private final UserEventInterceptorFactory mUserEventInterceptorFactory;
  private final EventManager                mEventManager;
  private final EventRouter                 mEventRouter;
  private final Priority                    mPriority;

  @Inject
  public UserEventsDestination(
    Provisioning provisioning,
    UserEventInterceptorFactory userEventInterceptorFactory,
    EventRouter eventRouter,
    EventManager eventManager
  )
  {
    mProvisioning = provisioning;
    mUserEventInterceptorFactory = userEventInterceptorFactory;
    mEventRouter = eventRouter;
    mEventManager = eventManager;
    mPriority = new Priority(3);
  }

  @Override
  public boolean canHandle(SpecificAddress address)
  {
    try
    {
      Account account = mProvisioning.getAccountByName(address.toString());
      if (account != null)
      {
        return mProvisioning.onLocalServer(account);
      }
      return false;
    }
    catch (Exception ex)
    {
      ChatLog.log.warn("Exception: " + Utils.exceptionToString(ex));
      return false;
    }
  }

  @Override
  public void deliverEvent(Event event, SpecificAddress address) {
    EventInterceptor interceptor = event.interpret(mUserEventInterceptorFactory);
    try {
      interceptor.intercept(mEventManager, address);
    } catch (Exception ex) {
      ChatLog.log.err("Error: " + Utils.exceptionToString(ex));
    }
  }

  @Override
  public Collection<? extends EventDestination> getDestinations(SpecificAddress address) {
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
  }

  @Override
  public void stop()
  {
    mEventRouter.unplugDestinationProvider(this);
  }
}
