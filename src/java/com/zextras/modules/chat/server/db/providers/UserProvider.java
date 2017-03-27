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

package com.zextras.modules.chat.server.db.providers;

import com.zextras.modules.chat.server.User;
import com.zextras.modules.chat.server.UserVisitor;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.exceptions.ChatDbException;

public interface UserProvider
{
  int INVALID_USER_ID = -1;

  User getUser(SpecificAddress address)
    throws ChatDbException;

  void visitAllUsers(UserVisitor visitor) throws ChatDbException;

  User getUserFromCache(SpecificAddress address);

  // Used only for full clear of chat
  void clearCache();

  boolean isLocal(SpecificAddress address);
}
