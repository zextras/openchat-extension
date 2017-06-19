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

package com.zextras.modules.chat.server.events;

//import com.zextras.annotations.VisibleForTesting;
import org.openzal.zal.lib.Clock;
import org.openzal.zal.lib.ActualClock;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.encoding.Encoder;
import com.zextras.modules.chat.server.encoding.EncoderFactory;
import com.zextras.modules.chat.server.interceptors.EventInterceptor;

public class EventMessageBack extends Event
{
  public EventMessageBack(
    EventId eventId,
    SpecificAddress sender,
    SpecificAddress messageTo,
    String message
  )
  {
    this(eventId, sender, messageTo, message, ActualClock.sInstance);
  }

  //@VisibleForTesting
  public EventMessageBack(
    EventId eventId,
    SpecificAddress sender,
    SpecificAddress target,
    String message,
    Clock clock
  )
  {
    super(eventId, sender, new Target(sender), clock);
    mSender = sender;
    mMessageTo = target;
    mMessage = message;
  }

  private final SpecificAddress mSender;
  private final SpecificAddress mMessageTo;
  private final String mMessage;

  public String getMessage()
  {
    return mMessage;
  }

  public SpecificAddress getMessageTo()
  {
    return mMessageTo;
  }


  public SpecificAddress getSender()
  {
    return mSender;
  }

  @Override
  public <T> T interpret(EventInterpreter<T> interpreter)
  {
    return interpreter.interpret(this);
  }
}

