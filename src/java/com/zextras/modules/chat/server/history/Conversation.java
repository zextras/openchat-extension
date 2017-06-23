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

package com.zextras.modules.chat.server.history;

import com.zextras.modules.chat.server.ChatMessage;
import com.zextras.modules.chat.server.address.SpecificAddress;
import org.openzal.zal.Account;

import java.util.LinkedList;
import java.util.List;

public class Conversation
{
  private final Account           mAccount;
  private final SpecificAddress   mParticipant;
  private final List<ChatMessage> mMessages;

  public Conversation(Account account, SpecificAddress participant)
  {
    mAccount = account;
    mParticipant = participant;
    mMessages = new LinkedList<ChatMessage>();
  }

  public void add(ChatMessage message)
  {
    mMessages.add(message);
  }

  public List<ChatMessage> getMessages()
  {
    return mMessages;
  }

  public Account getAccount() {
    return mAccount;
  }

  public SpecificAddress getParticipant() {
    return mParticipant;
  }
}
