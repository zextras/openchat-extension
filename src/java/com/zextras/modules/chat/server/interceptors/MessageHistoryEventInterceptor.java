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

import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.properties.ChatProperties;
import com.zextras.modules.chat.server.ChatMessage;
import com.zextras.modules.chat.server.history.ImHistoryQueue;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.EventManager;
import com.zextras.modules.chat.server.events.EventMessage;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import org.openzal.zal.Account;
import org.openzal.zal.Provisioning;
import org.openzal.zal.exceptions.ZimbraException;


public class MessageHistoryEventInterceptor implements EventInterceptor
{
  private final EventMessage   mEventMessage;
  private final Provisioning   mProvisioning;
  private final ChatProperties mChatProperties;
  private final ImHistoryQueue mImHistoryQueue;

  public MessageHistoryEventInterceptor(
    EventMessage eventMessage,
    Provisioning provisioning,
    ChatProperties chatProperties,
    ImHistoryQueue imHistoryQueue
  )
  {
    mEventMessage = eventMessage;
    mProvisioning = provisioning;
    mChatProperties = chatProperties;
    mImHistoryQueue = imHistoryQueue;
  }

  @Override
  public boolean intercept(EventManager eventManager, SpecificAddress target)
    throws ChatException, ChatDbException, ZimbraException
  {
    if (mChatProperties.isChatHistoryEnabled(target.toString()))
    {
      try
      {
        Account account = mProvisioning.assertAccountByName(target.toString());
        Account sender = mProvisioning.getAccountByName(mEventMessage.getSender().toString());

        ChatMessage chatMessage = new ChatMessage(mEventMessage.getSender(), target);
        chatMessage.setBody(mEventMessage.getMessage());

        if (sender != null)
        {
          chatMessage.setSenderName(sender.getName());
        }
        else
        {
          chatMessage.setSenderName(mEventMessage.getSender().toString());
        }

        chatMessage.setTargetName(account.getName());

        if (mProvisioning.onLocalServer(account))
        {
          mImHistoryQueue.addMessage(chatMessage, account);
          return true;
        }
      }
      catch (Exception e)
      {
        ChatLog.log.warn("Cannot save history for " + target.withoutSession().toString() + ": " + e.getMessage());
        return true;
      }
    }
    return false;
  }
}
