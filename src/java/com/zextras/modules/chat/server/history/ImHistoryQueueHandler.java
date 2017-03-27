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

package com.zextras.modules.chat.server.history;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.zextras.lib.log.ChatLog;
import com.zextras.lib.services.ThreadService;
import com.zextras.modules.chat.OpenChatModule;
import com.zextras.modules.chat.server.address.SpecificAddress;
import org.openzal.zal.Account;
import org.openzal.zal.AccountStatus;
import org.openzal.zal.Mailbox;
import org.openzal.zal.MailboxManager;
import org.openzal.zal.OperationContext;
import org.openzal.zal.Utils;
import org.openzal.zal.exceptions.AlreadyInProgressException;
import org.openzal.zal.exceptions.QuotaExceededException;
import org.openzal.zal.exceptions.ZimbraException;

import java.util.*;

public class ImHistoryQueueHandler extends ThreadService
{
  private final ImHistoryQueue            mQueue;
  private final MailboxManager            mMailboxManager;
  private final HistoryMailManagerFactory mHistoryMailManagerFactory;

  @Inject
  public ImHistoryQueueHandler(
    MailboxManager mailboxManager,
    HistoryMailManagerFactory historyMailManagerFactory,
    @Assisted ImHistoryQueue queue
  )
  {
    mQueue = queue;
    mMailboxManager = mailboxManager;
    mHistoryMailManagerFactory = historyMailManagerFactory;
  }

  @Override
  public void run()
  {
    while (!isStopped())
    {
      try
      {
//        mLog.chat.info("Getting all messages in queue");
        List<ImHistoryItem> queue = mQueue.getMessages();
        if (isStopped())
        {
          break;
        }
//        mLog.chat.info("Grouping conversations for history log");
        Collection<Conversation> conversations = buildConversations(queue);
        for (Conversation conversation : conversations)
        {
          //          mLog.chat.info("Saving instant messages in history log");
          Account account = conversation.getAccount();
          /* if (mConfigProvider.getAccountConfig(account).isHistoryEnabled())
          { */
            OperationContext operationContext = new OperationContext(account);
            Mailbox mbox = mMailboxManager.getMailboxByAccount(account);
            if (!AccountStatus.ACCOUNT_STATUS_MAINTENANCE.equals(account.getAccountStatusAsString()))
            {
              HistoryMailManager historyMailManager = mHistoryMailManagerFactory.create(mbox, operationContext);
              historyMailManager.updateHistoryMail(conversation);
            }
            else
            {
              ChatLog.log.debug(this, "Skipping history for account " + account.getName() + " in maintenance mode.");
            }
          /* } */
        }
      }
      catch (InterruptedException ignored) {}
      catch (AlreadyInProgressException ex)
      {
          ChatLog.log.debug("Unable re-index chat log: indexing already in progress. Exception: " +
                             Utils.exceptionToString(ex));
      }
      catch (QuotaExceededException ex)
      {
        ChatLog.log.warn("Unable to write chat log: Quota Exceeded.");
        ChatLog.log.debug("Exception: " + Utils.exceptionToString(ex));
      }
      catch (ZimbraException ex)
      {
        ChatLog.log.err("Unable to write chat log: Unknown Exception.");
        ChatLog.log.debug("Exception: " + Utils.exceptionToString(ex));
        try
        {
          //avoid log wasting
          Thread.sleep(1000L);
        }
        catch (InterruptedException ignored){
        }
      }
      catch (Throwable ex)
      {
        ChatLog.log.err("Chat history exception: " + Utils.exceptionToString(ex));
        try
        {
          //avoid log wasting
          Thread.sleep(60000L);
        }
        catch (InterruptedException ignored){
        }
      }
    }
  }

  @Override
  public String getLoggerName()
  {
    return OpenChatModule.MODULE_NAME + " History Handler Thread";
  }

  @VisibleForTesting
  public Collection<Conversation> buildConversations(List<ImHistoryItem> messages)
  {
    Map<Participants, Conversation> conversations = new HashMap<Participants, Conversation>();
    for (ImHistoryItem message : messages)
    {
      Conversation conversation;
      SpecificAddress self = new SpecificAddress(message.getAccount().getName());
      SpecificAddress friend = message.getChat().getMessageTo();

      if (self.equals(friend))
      {
        friend = message.getChat().getSender();
      }

      Participants participants = new Participants(self, friend);
      if (conversations.containsKey(participants))
      {
        conversation = conversations.get(participants);
      }
      else
      {
        conversation = new Conversation(message.getAccount(), participants.getRecipient());
      }
      conversation.add(message.getChat());
      conversations.put(participants, conversation);
    }
    return conversations.values();
  }
}
