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

package com.zextras.modules.chat;

import com.zextras.modules.chat.server.ChatMessage;
import com.zextras.modules.chat.server.relationship.Relationship;
import com.zextras.modules.chat.server.User;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.exceptions.ChatDbException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.StringEscapeUtils;

public class ConversationBuilder
{
  private        String          mUsername;
  private        UserProvider    mOpenUserProvider;
  private static UserProvider    sOpenUserProvider;
  private        StringBuilder[] mConversation;

  private final int TEXT_PART = 0;
  private final int HTML_PART = 1;

  private final SimpleDateFormat mHourFormatter;

  private Date mConversationDate;
  private String mLastUser = "";
  private Boolean mSentByMe = false;
  private boolean mIsNewMessage;

  /**
   * Header used to build ZxChat history/notifications mails (HTML section).
   */
  private static String sConversationHTMLHeader = "<html>\n" +
    "\t<style>\n" +
    "\t\tbody {}\n" +
    "\t\tp {}\n" +
    "\t\t.ZxChat_msg_user { display: table-cell; position: relative; bottom: 0; }\n" +
    "\t\t.ZxChat_msg_date { display: table-cell; width: 5%; position: relative; color: #989F9F; bottom: 0; font-weight: normal; padding-right:5px;}\n" +
    "\t\t.ZxChat_msg_line { display: table-row; position: relative; width: 100%; }\n" +
    "\t\t.ZxChat_msg_text { display: table-cell; width: 85%; text-indent: 0px; position: relative; padding-left: 10px;}\n" +
    "\t\t.ZxChat_msg_container { display: table; width: 100%; }\n" +
    "\t\t.ZxChat_msg_container_first { display: table; border-top: 1px solid #989F9F; width: 100%;}\n" +
    "\t\t.ZxChat_msg_text p, .ZxChat_msg_text h1, .ZxChat_msg_text h2, .ZxChat_msg_text h3, " +
    ".ZxChat_msg_text h4, .ZxChat_msg_text h5, .ZxChat_msg_text h6, .ZxChat_msg_text ul, " +
    ".ZxChat_msg_text ol { margin: 0; }\n" +
    "\t\t.ZxChat_msg_text ul, .ZxChat_msg_text ol { padding-left: 2em; }\n" +
    "\t\t.emojione { /* Emoji Sizing */ font-size: inherit; height: 18px; width: 18px; min-height: 18px; min-width: 18px; /* Inline alignment adjust the margins  */ display: inline-block; /*margin: -.4ex .15em .2ex;*/ line-height: normal; vertical-align: middle; }\n" +
    "\t\timg.emojione { /* prevent img stretch */ width: 18px; height: 18px; }\n"+
    "\t\t.ZxChat_msg_text ul li, .ZxChat_msg_text ol li {}\n" +
          ".ZxChat_msg_date { color: #989F9F; text-align: left; }\n" +
          ".ZxChat_msg_user { font-weight:bold; text-align: right; }\n" +
          ".ZxChat_msg_text { text-align: left; }\n" +
    "\t</style>\n" +
    "\t<body>\n";

  /**
   * Footer used to build ZxChat history/notifications mails (HTML section).
   */
  private static String sConversationHTMLFooter = "\t</body>\n</html>";


  public ConversationBuilder(String username, UserProvider openUserProvider)
  {
    this(username, null, openUserProvider);
  }

  public ConversationBuilder(String username, ChatMessage conversation, UserProvider openUserProvider)
  {
    mUsername = username;
    mOpenUserProvider = openUserProvider;
    sOpenUserProvider = openUserProvider;
    mLastUser = "";

    /* Build all conversations:
     *   Conversations are store in StringBuilder arrays
     *   Position 0 contains the conversation as text
     *   Position 1 contains the conversation as HTML
     */
    mConversation = new StringBuilder[2];
    mConversation[TEXT_PART] = new StringBuilder();
    mConversation[HTML_PART] = new StringBuilder();
    mHourFormatter = new SimpleDateFormat("HH:mm");

    if(conversation != null)
    {
      mIsNewMessage = false;
      mConversation[TEXT_PART].append(conversation.getBody());

      if(!conversation.getHtmlBody().contains("<style>"))
      {
        mConversation[HTML_PART].append(sConversationHTMLHeader);
        mIsNewMessage = true;
      }
      mConversation[HTML_PART].append(conversation.getHtmlBody());
      mLastUser = getLastUser();

    }
    else
    {
      mIsNewMessage = true;
      mConversation[HTML_PART].append(sConversationHTMLHeader);
    }

  }

  public void addMessage(ChatMessage chatMessage, TimeZone timezone)
  {
    mConversationDate = chatMessage.getCreationDate();
    addMessage(chatMessage, mConversationDate, timezone);

  }

  public void addMessage(ChatMessage chatMessage, Date conversationDate, TimeZone timezone)
  {
    mConversationDate = conversationDate;
    Date messageDate = chatMessage.getCreationDate();

    String sender = chatMessage.getSender().toString();
    String nickname = sender;

    mSentByMe = sender.equals(mUsername);
    if(!mSentByMe)
    {
      mSentByMe = sender.equals("Me");
    }
    Boolean isFirstMessage = (!sender.equals(mLastUser) && (mConversation[TEXT_PART].length() > 0));

    if (!mSentByMe)
    {
      try
      {
        User user = mOpenUserProvider.getUser(new SpecificAddress(mUsername));
        if (user.hasRelationship(chatMessage.getSender()))
        {
          Relationship recipient = user.getRelationship(chatMessage.getSender());
          nickname = recipient.getBuddyNickname();
        }
      }
      catch (ChatDbException ignored) {}
    }
    else
    {
      nickname = "Me";
    }

    mHourFormatter.setTimeZone(timezone);
    String formattedDate = mHourFormatter.format(messageDate);
    String formattedUser = nickname;

    // remove all [ hour ]. Example: "[ 10:00 ]"
    String conversationText = chatMessage.getBody();
    conversationText = conversationText.replaceFirst("Me:", "");

    // remove all e-mail address + :. Example: "simple@example.com:"
    conversationText = conversationText.replaceFirst("\\[.*?\\] [A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]+:", "");

    String messageHTML;
    messageHTML = StringEscapeUtils.escapeHtml4(conversationText).replaceAll("\n", "<br>");

    appendMessage(
      conversationText,
      messageHTML,
      isFirstMessage,
      formattedUser,
      formattedDate
    );
    mLastUser = formattedUser;
  }

  public String getHTMLMessage()
  {
    return mConversation[HTML_PART].toString();
  }

  public void closeHtml()
  {
    mConversation[HTML_PART].append(sConversationHTMLFooter);
  }

  public String getTextMessage()
  {
    return mConversation[TEXT_PART].toString();
  }

  public Date getConversationDate()
  {
    return mConversationDate;
  }

  private void appendMessage(String TextMessage ,String htmlMessage, boolean isFirstMessage, String formattedUser, String formattedDate)
  {

    StringBuilder conversationHtml = mConversation[HTML_PART];
    StringBuilder conversationText = mConversation[TEXT_PART];

    if(mIsNewMessage)
    {
      conversationHtml.append("\t\t<div class=\"")
                      .append(isFirstMessage ? "ZxChat_msg_container_first" : "ZxChat_msg_container")
                      .append("\">\n");
    }
    else
    {
      int ind = conversationHtml.toString().lastIndexOf("</div>");
      if( ind >= 0 )
      {
        conversationHtml = new StringBuilder( conversationHtml.toString().subSequence(0, ind - 1 ) );
      }
    }

    conversationHtml.append("\t\t\t<div class=\"ZxChat_msg_line\">")
                    .append("\t\t\t<div class=\"ZxChat_msg_date\">").append(formattedDate).append("\n\t\t\t</div>\n")
                    .append((!formattedUser.equals(mLastUser)) ? ("\t\t\t<div class=\"ZxChat_msg_user\">" + formattedUser + ":</div>\n") : "\t\t\t<div class=\"ZxChat_msg_user\"> </div>\n")
                    .append("\t\t\t<div class=\"ZxChat_msg_text\">\n\t\t\t\t")
                    .append(htmlMessage)
                    .append("\n\t\t\t</div>\n")
                    .append("\n\t\t\t</div>\n")
                    .append("\t\t</div>\n");


    conversationText.append("[ ").append(formattedDate).append(" ] ")
                    .append(formattedUser).append(": ")
                    .append(TextMessage)
                    .append("\n\n");

    mConversation[HTML_PART] = conversationHtml;
    mConversation[TEXT_PART] = conversationText;
    mIsNewMessage = false;
  }

  public String getLastUser()
  {
    if(!mConversation[HTML_PART].toString().isEmpty())
    {
      int offset_ref = mConversation[HTML_PART].toString().lastIndexOf("<div class=\"ZxChat_msg_user\">");

      if(offset_ref >= 0)
      {
        String name = mConversation[HTML_PART].toString().substring(offset_ref + 29);
        String partOfHtml = mConversation[HTML_PART].toString().substring(0, offset_ref);
        offset_ref = name.indexOf("</div>");
        if(offset_ref >= 0)
        {
          name =  name.substring(0,offset_ref );

          while(name.isEmpty() || name.equals(" ") )
          {
            offset_ref = partOfHtml.lastIndexOf("<div class=\"ZxChat_msg_user\">");
            if(offset_ref >= 0)
            {
              name = partOfHtml.substring(offset_ref + 29);
              partOfHtml = partOfHtml.substring(0, offset_ref);

            }

            offset_ref = name.indexOf("</div>");
            name =  name.substring(0, offset_ref);
          }
        }
        else
        {
          name = "";
        }
        return name.replaceAll(":","");
      }
    }
    return "";
  }
}
