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
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.events.*;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.session.Session;
import com.zextras.modules.chat.server.session.SessionManager;
import org.openzal.zal.Provisioning;
import org.openzal.zal.exceptions.ZimbraException;

import java.util.List;


@Singleton
public class UserEventInterceptorFactory extends StubEventInterceptorFactory
{
  private       UserProvider   mOpenUserProvider;
  private final SessionManager mSessionManager;
  private final Provisioning   mProvisioning;

  @Inject
  public UserEventInterceptorFactory(
    UserProvider openUserProvider,
    SessionManager sessionManager,
    Provisioning provisioning
  )
  {
    mOpenUserProvider = openUserProvider;
    mSessionManager = sessionManager;
    mProvisioning = provisioning;
  }

  @Override
  public EventInterceptor interpret(EventFriendAccepted eventFriendAccepted)
  {
    return new FriendAcceptedEventInterceptor(mOpenUserProvider, eventFriendAccepted, mProvisioning);
  }

  @Override
  public EventInterceptor interpret(EventFriendAdded eventFriendAdded)
  {
    return new FriendAddedEventInterceptor(mOpenUserProvider, eventFriendAdded, mProvisioning);
  }

  @Override
  public EventInterceptor interpret(EventStatusProbe eventStatusProbe)
  {
    return new StatusProbeInterceptor(mOpenUserProvider, mSessionManager, eventStatusProbe);
  }

  @Override
  public EventInterceptor interpret(final EventFloodControl event)
  {
    return new EventInterceptor()
    {
      @Override
      public boolean intercept(EventManager eventManager, SpecificAddress target) throws ChatException, ChatDbException, ZimbraException
      {
        List<Session> sessions = mSessionManager.getUserSessions(
          new SpecificAddress(event.getTarget().toSingleAddressIncludeResource())
        );

        for (Session session : sessions)
        {
          if (event.isFloodDetected())
          {
            session.refuseInputEvents();
          }
          else
          {
            session.acceptInputEvents();
          }
        }

        return true;
      }
    };
  }
}
