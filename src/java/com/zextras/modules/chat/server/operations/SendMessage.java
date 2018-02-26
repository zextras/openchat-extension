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

package com.zextras.modules.chat.server.operations;

import com.zextras.modules.chat.properties.ChatProperties;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.events.*;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.session.SessionManager;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.exceptions.ChatException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SendMessage implements ChatOperation
{
  private final EventId         mEventId;
  private final SpecificAddress mSenderAddress;
  private final SpecificAddress mTarget;
  private       String          mMessage;
  private final long            mTimestamp;

  public SendMessage(SpecificAddress senderAddress, SpecificAddress target, String message)
  {
    this(EventId.randomUUID(), senderAddress, target, message, System.currentTimeMillis());
  }

  public SendMessage(EventId eventId, SpecificAddress senderAddress, SpecificAddress target, String message,long timestamp)
  {
    mEventId = eventId;
    mSenderAddress = senderAddress;
    mTarget = target;
    mMessage = message;
    mTimestamp = timestamp;
  }

  @Override
  public List<Event> exec(SessionManager sessionManager, UserProvider userProvider)
    throws ChatException, ChatDbException
  {
    if (mSenderAddress.equals(mTarget))
    {
      return Collections.emptyList();
    }

    ArrayList<Event> eventList = new ArrayList<Event>(3);

    if (mMessage.length() >= ChatProperties.MAX_MESSAGE_SIZE)
    {
      Event messageSizeExceeded = new EventMessageSizeExceeded(
        mEventId,
        mSenderAddress,
        mTarget,
        mMessage.length()
      );
      eventList.add(messageSizeExceeded);

      mMessage = mMessage.substring(0, ChatProperties.MAX_MESSAGE_SIZE);
    }

    Event sendMessageEvent = new EventMessage(
      mEventId,
      mSenderAddress,
      new Target(mTarget),
      mMessage,
      mTimestamp
    );

    EventMessageBack messageBack = new EventMessageBack(
      mEventId,
      mSenderAddress,
      mTarget,
      mMessage
    );

    eventList.add(sendMessageEvent);
    eventList.add(messageBack);

    return eventList;
  }
}
