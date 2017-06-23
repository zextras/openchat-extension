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

package com.zextras.modules.chat.server;

import com.zextras.modules.chat.properties.ChatProperties;
import org.openzal.zal.*;
import org.openzal.zal.Flag;
import org.openzal.zal.exceptions.ZimbraException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

public class ChatMessageWriter
{
  private final ChatProperties mChatProperties;
  private final Mailbox mMbox;
  private final String           mHtml;
  private final String           mPlainText;
  private final Date             mConversationDate;
  private final OperationContext mOperationContext;

  public ChatMessageWriter(
    ChatProperties chatProperties,
    Mailbox mbox,
    OperationContext operationContext,
    String html,
    String plainText,
    Date conversationDate
  )
    throws ZimbraException
  {
    mChatProperties = chatProperties;
    mMbox = mbox;
    mHtml = html;
    mPlainText = plainText;
    mConversationDate = conversationDate;
    mOperationContext = operationContext;
  }

  public void updateExistingChat(Chat chat)
    throws ZimbraException, MessagingException, IOException
  {
    MimeMessage currentMime = chat.getMimeMessage();
    Multipart mp = new MimeMultipart("alternative");

    MimeBodyPart textPart = new MimeBodyPart();

    textPart.setHeader("Content-Type", "text/plain; charset=utf-8");
    textPart.setContent(mPlainText, "text/plain; charset=utf-8");

    MimeBodyPart htmlPart = new MimeBodyPart();
    htmlPart.setHeader("Content-Type", "text/html; charset=utf-8");
    htmlPart.setContent(mHtml, "text/html; charset=utf-8");

    mp.addBodyPart(textPart);
    mp.addBodyPart(htmlPart);

    currentMime.setContent(mp);
    currentMime.saveChanges();

    mMbox.updateChat(
      mOperationContext,
      new ParsedMessage(currentMime, true),
      chat.getId()
    );

    mMbox.reindexItem(new Item(chat));
  }

  public void createNewChat(String sender, String version) throws MessagingException, ZimbraException, IOException {

    InternetAddress from = new InternetAddress(sender);
    InternetAddress to = new InternetAddress(mMbox.getAccount().getName());

    MimeMessage mmsg = new MimeMessage(javax.mail.Session.getInstance(new Properties()));
    mmsg.setFrom(from);
    mmsg.setRecipient(Message.RecipientType.TO, to);
    mmsg.setSubject(mChatProperties.getProductName() + " - " + from.toString());
    mmsg.setSentDate(mConversationDate);
    mmsg.setHeader("ZxChat-History-Version", version);

    Multipart mp = new MimeMultipart("alternative");
    MimeBodyPart textPart = new MimeBodyPart();
    textPart.setHeader("Content-Type", "text/plain; charset=utf-8");
    textPart.setContent(mPlainText, "text/plain; charset=utf-8");


    MimeBodyPart htmlPart = new MimeBodyPart();
    htmlPart.setHeader("Content-Type", "text/html; charset=utf-8");
    htmlPart.setContent(mHtml, "text/html; charset=utf-8");

    mp.addBodyPart(textPart);
    mp.addBodyPart(htmlPart);

    mmsg.setContent(mp);
    ParsedMessage pm = new ParsedMessage(mmsg, true);

    Item chat = mMbox.createChat(
      mOperationContext,
      pm,
      Mailbox.ID_FOLDER_IM_LOGS,
      Flag.BITMASK_FROM_ME
    );

    mMbox.reindexItem(chat);
  }
}
