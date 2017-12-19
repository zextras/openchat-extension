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
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.xmpp.AuthStatus;
import com.zextras.modules.chat.server.xmpp.XmppAuthentication;

import java.util.Collection;

public class EventIQAuthResult extends Event
{
  public AuthStatus getAuthStatus()
  {
    return mAuthStatus;
  }

  private final AuthStatus mAuthStatus;
  private final Collection<XmppAuthentication> mAvailableAuthentications;

  public EventIQAuthResult(EventId eventId, AuthStatus authStatus, Collection<XmppAuthentication> availableAuthentications)
  {
    super(eventId, new SpecificAddress("fake@example.com"), new Target());
    mAuthStatus = authStatus;
    mAvailableAuthentications = availableAuthentications;
  }

  public Collection<XmppAuthentication> getAvailableAuthentications()
  {
    return mAvailableAuthentications;
  }

  @Override
  public <T> T interpret(EventInterpreter<T> interpreter) throws ChatException
  {
    return interpreter.interpret(this);
  }
}
