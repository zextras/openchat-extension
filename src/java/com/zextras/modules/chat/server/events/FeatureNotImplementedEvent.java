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

import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.NoneAddress;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.xmpp.parsers.IQRequestType;

public class FeatureNotImplementedEvent extends Event
{
  private final String        mOriginalSender;
  private final IQRequestType mRequestType;
  private final String        mRequestId;
  private final String        mOriginalReceiver;

  public FeatureNotImplementedEvent(
    String originalSender,
    String originalReceiver,
    IQRequestType requestType,
    String requestId
  )
  {
    super(new NoneAddress(), new Target(new SpecificAddress(originalSender)));
    mOriginalSender = originalSender;
    mOriginalReceiver = originalReceiver;
    mRequestType = requestType;
    mRequestId = requestId;
  }

  public String getOriginalSender()
  {
    return mOriginalSender;
  }

  public String getOriginalReceiver()
  {
    return mOriginalReceiver;
  }

  public String getRequestId()
  {
    return mRequestId;
  }

  @Override
  public <T> T interpret(EventInterpreter<T> interpreter) throws ChatException
  {
    return interpreter.interpret(this);
  }

  public boolean isResponseRequired()
  {
    return (IQRequestType.GET.equals(mRequestType) || IQRequestType.SET.equals(mRequestType)) && !mRequestId.isEmpty();
  }
}
