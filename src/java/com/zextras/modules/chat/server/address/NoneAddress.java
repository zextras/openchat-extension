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

package com.zextras.modules.chat.server.address;

import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.dispatch.ServerHostSetProvider;
import com.zextras.modules.chat.server.events.EventRouter;
import com.zextras.modules.chat.server.session.SessionUUID;
import com.zextras.modules.chat.server.dispatch.Dispatcher;
import com.zextras.modules.chat.server.dispatch.NoneDispatcher;

public class NoneAddress implements ChatAddress
{
  public static final NoneAddress sInstance = new NoneAddress();

  @Override
  public Dispatcher createDispatcher(EventRouter eventRouter, UserProvider openUserProvider, ServerHostSetProvider roomServerHostSetProvider)
  {
    return NoneDispatcher.sNoneDispatcher;
  }

  @Override
  public String resource()
  {
    return "";
  }

  @Override
  public String resourceAddress()
  {
    return "";
  }

  public boolean equals(Object object)
  {
    return object instanceof NoneAddress;
  }

  @Override
  public int hashCode()
  {
    return 0;
  }

  @Override
  public ChatAddress withoutSession()
  {
    return this;
  }

  @Override
  public ChatAddress withoutResource()
  {
    return this;
  }

  @Override
  public boolean isFromSession(SessionUUID sessionUUID)
  {
    return false;
  }
}
