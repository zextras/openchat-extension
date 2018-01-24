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
import com.zextras.lib.json.JSONObject;
import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.properties.ChatProperties;
import com.zextras.modules.chat.server.ImMessage;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.sql.ImMessageStatements;
import com.zextras.modules.chat.server.events.EventManager;
import com.zextras.modules.chat.server.events.EventMessage;
import com.zextras.modules.chat.server.events.EventMessageBack;
import com.zextras.modules.chat.server.events.EventMessageHistory;
import com.zextras.modules.chat.server.events.EventMessageHistoryLast;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.history.ImHistoryQueue;
import org.openzal.zal.Account;
import org.openzal.zal.Provisioning;
import org.openzal.zal.exceptions.ZimbraException;

public class UserHistoryInterceptorFactoryImpl2 extends StubEventInterceptorFactory implements UserHistoryInterceptorFactory
{
  private final Provisioning   mProvisioning;
  private final ChatProperties mChatProperties;
  private final ImMessageStatements mImMessageStatements;

  @Inject
  public UserHistoryInterceptorFactoryImpl2(
    Provisioning provisioning,
    ChatProperties chatProperties,
    ImMessageStatements imMessageStatements
  )
  {
    mProvisioning = provisioning;
    mChatProperties = chatProperties;
    mImMessageStatements = imMessageStatements;
  }


  @Override
  public EventInterceptor interpret(EventMessage eventMessage)
  {
    return new MessageHistoryEventInterceptor(
      eventMessage,
      mProvisioning,
      mChatProperties,
      mImMessageStatements
    );
  }

  @Override
  public EventInterceptor interpret(EventMessageBack eventMessage)
  {
    return new MessageHistoryBackEventInterceptor(
      eventMessage,
      mProvisioning,
      mChatProperties,
      mImMessageStatements
    );
  }

  @Override
  public EventInterceptor interpret(EventMessageHistory eventMessage)
  {
    System.out.println("EventMessageHistory");
    return new StubEventInterceptor();
  }

  @Override
  public EventInterceptor interpret(EventMessageHistoryLast eventMessage)
  {
    System.out.println("Last");
    return new StubEventInterceptor();
  }

  public class MessageHistoryEventInterceptor implements EventInterceptor
  {
    private final EventMessage      mEventMessage;
    private final Provisioning      mProvisioning;
    private final ChatProperties    mChatProperties;
    private final ImMessageStatements mImMessageStatements;

    public MessageHistoryEventInterceptor(
      EventMessage eventMessage,
      Provisioning provisioning,
      ChatProperties chatProperties,
      ImMessageStatements imMessageStatements
    )
    {
      mEventMessage = eventMessage;
      mProvisioning = provisioning;
      mChatProperties = chatProperties;
      mImMessageStatements = imMessageStatements;
    }

    @Override
    public void intercept(EventManager eventManager, SpecificAddress target)
      throws ChatException, ChatDbException, ZimbraException
    {
      if (mChatProperties.isChatHistoryEnabled(target.toString()))
      {
        try
        {
          Account account = mProvisioning.assertAccountByName(target.toString());

          ImMessage message = new ImMessage(
            mEventMessage.getId().toString(),
            mEventMessage.getSender().withoutResource().toString(),
            target.withoutResource().toString(),
            mEventMessage.getMessage());

          if (mProvisioning.onLocalServer(account))
          {
            mImMessageStatements.insert(message);
          }
        }
        catch (Exception e)
        {
          ChatLog.log.warn("Cannot save history for " + target.withoutSession().toString() + ": " + e.getMessage());
        }
      }
    }
  }

  public class MessageHistoryBackEventInterceptor implements EventInterceptor
  {
    private final EventMessageBack      mEventMessage;
    private final Provisioning      mProvisioning;
    private final ChatProperties    mChatProperties;
    private final ImMessageStatements mImMessageStatements;

    public MessageHistoryBackEventInterceptor(
      EventMessageBack eventMessage,
      Provisioning provisioning,
      ChatProperties chatProperties,
      ImMessageStatements imMessageStatements
    )
    {
      mEventMessage = eventMessage;
      mProvisioning = provisioning;
      mChatProperties = chatProperties;
      mImMessageStatements = imMessageStatements;
    }

    @Override
    public void intercept(EventManager eventManager, SpecificAddress target)
      throws ChatException, ChatDbException, ZimbraException
    {
      if (mChatProperties.isChatHistoryEnabled(target.toString()))
      {
        try
        {
          Account account = mProvisioning.assertAccountByName(target.toString());

          ImMessage message = new ImMessage(
            mEventMessage.getId().toString(),
            mEventMessage.getSender().withoutResource().toString(),
            mEventMessage.getMessageTo().withoutResource().toString(),
            mEventMessage.getMessage(),
            mEventMessage.getTimestamp());

          if (mProvisioning.onLocalServer(account))
          {
            message.setDelivered("delivered");
            mImMessageStatements.update(message);
          }
        }
        catch (Exception e)
        {
          ChatLog.log.warn("Cannot save history for " + target.withoutSession().toString() + ": " + e.getMessage());
        }
      }
    }
  }
}