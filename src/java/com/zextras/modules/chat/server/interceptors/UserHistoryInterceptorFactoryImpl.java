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

package com.zextras.modules.chat.server.interceptors;


import com.google.inject.Inject;
import com.zextras.modules.chat.properties.ChatProperties;
import com.zextras.modules.chat.server.history.ImHistoryQueue;
import com.zextras.modules.chat.server.events.*;
import org.openzal.zal.*;
import org.openzal.zal.Provisioning;

public class UserHistoryInterceptorFactoryImpl extends StubEventInterceptorFactory implements UserHistoryInterceptorFactory
{
  private final Provisioning   mProvisioning;
  private final ChatProperties mChatProperties;
  private ImHistoryQueue mImHistoryQueue;

  @Inject
  public UserHistoryInterceptorFactoryImpl(
    Provisioning provisioning,
    ChatProperties chatProperties,
    ImHistoryQueue imHistoryQueue
  )
  {
    mProvisioning = provisioning;
    mChatProperties = chatProperties;
    mImHistoryQueue = imHistoryQueue;
  }

  @Override
  public EventInterceptor interpret(final EventMessage eventMessage)
  {
    return new MessageHistoryEventInterceptor(
      eventMessage,
      mProvisioning,
      mChatProperties,
      mImHistoryQueue
    );
  }

  @Override
  public EventInterceptor interpret(final EventMessageBack eventMessage)
  {
    return new MessageHistoryBackEventInterceptor(
      eventMessage,
      mProvisioning,
      mChatProperties,
      mImHistoryQueue
    );
  }

}