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

import java.util.HashSet;

public interface ChatAddress
{
  Dispatcher createDispatcher(EventRouter eventRouter, UserProvider openUserProvider);

  String toString();
  String resource();
  String resourceAddress();

  @Deprecated
  void explode(HashSet<SpecificAddress> explodedSet, UserProvider openUserProvider) throws ChatDbException;

  ChatAddress withoutSession();
  ChatAddress withoutResource();

  boolean isFromSession( SessionUUID sessionUUID );

}
