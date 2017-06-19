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
import com.zextras.modules.chat.server.Relationship;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.*;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.filters.EventFilter;
import com.zextras.modules.chat.server.interceptors.EventInterceptor;
import java.util.concurrent.atomic.AtomicBoolean;

import com.zextras.modules.chat.server.interceptors.StubEventInterceptorFactory;
import org.openzal.zal.exceptions.ZimbraException;


public class CommonSessionEventFilterImpl implements CommonSessionEventFilter {
  private final EventManager mEventManager = null;
  private final CommonSessionEventInterceptorBuilder mCommonSessionEventInterceptorBuilder;

  @Inject
  public CommonSessionEventFilterImpl(CommonSessionEventInterceptorBuilder commonSessionEventInterceptorBuilder)
  {
    mCommonSessionEventInterceptorBuilder = commonSessionEventInterceptorBuilder;
  }

  @Override
  public boolean isFiltered(
    Event event,
    final SpecificAddress target,
    final Session session)
  {
    final AtomicBoolean settableBoolean = new AtomicBoolean();

    EventInterceptor eventInterceptor = event.interpret(
      mCommonSessionEventInterceptorBuilder.buildFactory(session, settableBoolean)
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
