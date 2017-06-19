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
import com.zextras.modules.chat.server.xmpp.XmppAuthentication;

import java.util.Collection;

public class EventXmppSessionFeatures extends Event
{
  private final boolean mBindable;

  public Collection<XmppAuthentication> getAvailableAuthentications()
  {
    return mAvailableAuthentications;
  }

  public boolean isBindable()
  {
    return mBindable;
  }

  private final Collection<XmppAuthentication> mAvailableAuthentications;

  public boolean isUsingSSL()
  {
    return mUsingSSL;
  }

  private final boolean mUsingSSL;

  public EventXmppSessionFeatures(
    SpecificAddress sender,
    boolean bindable,
    Collection<XmppAuthentication> availableAuthentications,
    boolean usingSSL
  )
  {
    super(sender, new Target());
    mBindable = bindable;
    mAvailableAuthentications = availableAuthentications;
    mUsingSSL = usingSSL;
  }

  public boolean sslRequired()
  {
    return false;
  }

  @Override
  public <T> T interpret(EventInterpreter<T> interpreter)
  {
    return interpreter.interpret(this);
  }
}
