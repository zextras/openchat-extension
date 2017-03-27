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

package com.zextras.modules.chat.server.session;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventManager;
import com.zextras.modules.chat.server.events.EventXmppPing;
import com.zextras.modules.chat.server.interceptors.EventInterceptor;
import com.zextras.modules.chat.server.interceptors.StubEventInterceptorFactory;
import com.zextras.modules.chat.server.soap.SoapFilter;

import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class SoapFilterImpl implements SoapFilter
{
  private final EventManager mEventManager;

  @Inject
  public SoapFilterImpl(EventManager eventManager)
  {
    mEventManager = eventManager;
  }

  @Override
  public boolean isFiltered(final Event event, SpecificAddress target, Session session)
  {
    final AtomicBoolean settableBoolean = new AtomicBoolean();

    EventInterceptor eventInterceptor = event.interpret(
      new StubEventInterceptorFactory(){
        public EventInterceptor interpret(EventXmppPing eventXmppPing){
          return new FilterEventInterceptor(settableBoolean, true);
        }
      }
    );

    try {
      eventInterceptor.intercept(mEventManager,target);
    } catch ( Throwable ex ) {
      RuntimeException newEx = new RuntimeException(ex);
      throw newEx;
    }

    return settableBoolean.get();
  }
}
