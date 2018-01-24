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


import com.zextras.modules.chat.server.address.SpecificAddress;


import java.util.Date;

@Deprecated
public class ChatMessage
{
  private SpecificAddress mSender;
  private SpecificAddress mMessageTo;
  private String mSenderName;
  private String mTargetName;
  private String mBody;
  private Date mCreationDate;
  private String mBodyHtml;
  private boolean mIsMessageBack;

  public ChatMessage(SpecificAddress sender, SpecificAddress messageTo)
  {
    this(sender, messageTo, new Date(System.currentTimeMillis()));
  }
  public ChatMessage(SpecificAddress sender, SpecificAddress messageTo, Date creationDate)
  {
    mSender = sender;
    mMessageTo = messageTo;
    mBody = "";
    mCreationDate = creationDate;
    mTargetName = messageTo.toString();
    mSenderName = sender.toString();
    mBodyHtml = "";
    mIsMessageBack = false;
  }

  public void setSenderName(String name)
  {
    mSenderName = name;
  }

  public void setTargetName(String name)
  {
    mTargetName = name;
  }

  public String getSenderName()
  {
    return mSenderName;
  }

  public String getTargetName()
  {
    return mTargetName;
  }

  public void setBody(String content)
  {
    mBody = content;
  }

  public SpecificAddress getSender()
  {
    return mSender;
  }

  public SpecificAddress getMessageTo()
  {
    return mMessageTo;
  }

  public String getBody()
  {
    return mBody;
  }

  public String getHtmlBody()
  {
    return mBodyHtml;
  }

  public void setHtmlBody(String html)
  {
    mBodyHtml = html;
  }

  public Date getCreationDate()
  {
    return mCreationDate;
  }


  public boolean isMessageBack()
  {
    return mIsMessageBack;
  }

  public void setIsMessageBack( boolean bool )
  {
    mIsMessageBack = bool;
  }
}
