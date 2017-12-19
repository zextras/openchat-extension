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

import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.session.SessionUUID;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.NoneAddress;
import com.zextras.modules.chat.server.xmpp.ConnectionType;
import com.zextras.modules.chat.server.xmpp.XmppError;

import java.util.Set;

public class EventStreamStarted extends Event
{
  public ConnectionType getClientType()
  {
    return mClientType;
  }

  public String getDomain()
  {
    return mDomain;
  }

  public Set<XmppError> getXmppErrors()
  {
    return mXmppErrors;
  }

  private final String         mDomain;
  private final Set<XmppError> mXmppErrors;
  private final SessionUUID    mSessionUUID;
  private final ConnectionType mClientType;


  public EventStreamStarted(SessionUUID sessionUUID, ConnectionType clientType, String domain, Set<XmppError> xmppErrors)
  {
    super(new NoneAddress(), new Target());
    mSessionUUID = sessionUUID;
    mClientType = clientType;
    mDomain = domain;
    mXmppErrors = xmppErrors;
  }

  public SessionUUID getSessionId()
  {
    return mSessionUUID;
  }

  @Override
  public <T> T interpret(EventInterpreter<T> interpreter) throws ChatException
  {
    return interpreter.interpret(this);
  }
}
