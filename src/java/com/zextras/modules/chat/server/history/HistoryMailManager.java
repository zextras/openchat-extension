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

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.zextras.modules.chat.ConversationBuilder;
import com.zextras.modules.chat.properties.ChatProperties;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import org.openzal.zal.Account;
import org.openzal.zal.Chat;
import org.openzal.zal.Item;
import org.openzal.zal.Mailbox;
import org.openzal.zal.OperationContext;
import org.openzal.zal.exceptions.ZimbraException;
import org.openzal.zal.lib.ActualClock;
import com.zextras.modules.chat.server.ChatMessage;
import com.zextras.modules.chat.server.ChatMessageWriter;
import com.zextras.modules.chat.server.address.SpecificAddress;
import org.jetbrains.annotations.Nullable;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


public class HistoryMailManager
{
  private       Mailbox          mMailbox;
  private       OperationContext mOperationContext;
  private final UserProvider     mOpenUserProvider;
  private final ChatProperties mChatProperties;
  private final String CHAT_HISTORY_VERSION = "1";

  @Inject
  public HistoryMailManager(
    @Assisted Mailbox mailbox,
    @Assisted OperationContext OperationContext,
    UserProvider openUserProvider,
    ChatProperties chatProperties
  )
  {
    mMailbox = mailbox;
    mOperationContext = OperationContext;
    mOpenUserProvider = openUserProvider;
    mChatProperties = chatProperties;
  }

  public void updateHistoryMail(Conversation conversation)
    throws ZimbraException, MessagingException, IOException
  {
    Date currentDate = getCurrentDate();
    List<ChatMessage> imHistoryItems = conversation.getMessages();
    Account self = conversation.getAccount();
    SpecificAddress participant = conversation.getParticipant();

    Chat chat = findMessage(currentDate, participant);
    if (chat != null)
    {
      updateConversation(imHistoryItems, chat, self, participant);
    }
    else
    {
      createNewConversation(imHistoryItems, self, participant);
    }
  }

  private void updateConversation(List<ChatMessage> messages, Chat chat, Account self, SpecificAddress participant)
    throws ZimbraException, MessagingException, IOException
  {
    ChatMessage conversation = new ChatMessage(new SpecificAddress(self.getName()), participant,  new Date(chat.getChangeDate()));

    Multipart multipart = (Multipart) chat.getMimeMessage().getDataHandler().getContent();

    for(int i = 0; i < multipart.getCount(); i++)
    {
      if (multipart.getBodyPart(i).isMimeType("text/plain"))
      {
        conversation.setBody(multipart.getBodyPart(i).getDataHandler().getContent().toString());
      }
      else
      {
        conversation.setHtmlBody(multipart.getBodyPart(i).getDataHandler().getContent().toString());
      }
    }

    ConversationBuilder conversationBuilder =
      new ConversationBuilder(self.getName(), conversation, mOpenUserProvider);

    for (ChatMessage message : messages)
    {
      conversationBuilder.addMessage(message, new Date(chat.getDate()), self.getAccountTimeZone().getTimeZone());
    }
    conversationBuilder.closeHtml();
    createMessageWriter(conversationBuilder).updateExistingChat(chat);
  }

  private void createNewConversation(List<ChatMessage> messages, Account self, SpecificAddress participant)
    throws ZimbraException, IOException, MessagingException
  {
    ConversationBuilder conversationBuilder =
      new ConversationBuilder(self.getName(), mOpenUserProvider);

    for (ChatMessage message : messages)
    {
      conversationBuilder.addMessage(message, self.getAccountTimeZone().getTimeZone());
    }
    conversationBuilder.closeHtml();
    createMessageWriter(conversationBuilder).createNewChat(participant.toString(), CHAT_HISTORY_VERSION);
  }

  public @Nullable
  Chat findMessage(Date currentDate, SpecificAddress participant)
    throws ZimbraException, MessagingException
  {
    List<Item> messages = mMailbox.getItemList(Item.TYPE_CHAT, mOperationContext);

    for( Item message : messages )
    {
      if ( message.getFolderId() != Mailbox.ID_FOLDER_IM_LOGS ) {
        continue;
      }
      Chat chat = message.toChat();
      String[] historyVersion = chat.getMimeMessage().getHeader("ZxChat-History-Version");
      Date emailDate = chat.getMimeMessage().getSentDate();

      if( historyVersion == null || emailDate == null ) {
        continue;
      }

      String subject = chat.getSubject();
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

      String currentDateFormatted = dateFormat.format(currentDate);
      String emailDateFormatted = dateFormat.format(emailDate);

      if( currentDateFormatted.equals(emailDateFormatted) && subject.endsWith("Chat - " + participant))
      {
        if (historyVersion.length > 0 && historyVersion[0].equals(CHAT_HISTORY_VERSION))
        {
          return chat;
        }
      }
    }

    return null;
  }

  public ChatMessageWriter createMessageWriter( ConversationBuilder conversationBuilder ) throws ZimbraException
  {
    return new ChatMessageWriter(
      mChatProperties,
      mMailbox,
      mOperationContext,
      conversationBuilder.getHTMLMessage(),
      conversationBuilder.getTextMessage(),
      conversationBuilder.getConversationDate()
    );
  }

  public Date getCurrentDate()
  {
    return new Date(ActualClock.sInstance.now());
  }
}
