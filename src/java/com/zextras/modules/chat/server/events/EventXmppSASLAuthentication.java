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
import com.zextras.modules.chat.server.xmpp.AuthStatus;

public class EventXmppSASLAuthentication extends Event
{
  private final AuthStatus mAuthStatus;
  private String mUsername;

  public EventXmppSASLAuthentication(String username, AuthStatus authStatus)
  {
    super(new NoneAddress(), new Target());
    mUsername = username;
    mAuthStatus = authStatus;
  }

  public AuthStatus getAuthStatus()
  {
    return mAuthStatus;
  }

  public String getUsername()
  {
    return mUsername;
  }

  @Override
  public <T> T interpret(EventInterpreter<T> interpreter)
  {
    return interpreter.interpret(this);
  }
}
