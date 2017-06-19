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
import com.zextras.modules.chat.server.events.EventRouter;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.session.SessionUUID;
import com.zextras.modules.chat.server.dispatch.Dispatcher;
import com.zextras.modules.chat.server.dispatch.NoneDispatcher;

import java.util.HashSet;

public class NoneAddress implements ChatAddress
{
  @Override
  public Dispatcher createDispatcher(EventRouter eventRouter, UserProvider openUserProvider)
  {
    return new NoneDispatcher();
  }

  @Override
  public String resourceAddress()
  {
    return "";
  }

  @Override
  public void explode(HashSet<SpecificAddress> explodedSet, UserProvider openUserProvider) throws ChatDbException
  {
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
  public boolean isFromSession(SessionUUID sessionUUID)
  {
    return false;
  }
}
