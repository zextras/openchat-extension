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

package com.zextras.modules.chat.server.interceptors;

import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.EventManager;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import org.openzal.zal.exceptions.ZimbraException;


public interface EventInterceptor
{
  boolean intercept(EventManager eventManager, SpecificAddress target)
    throws ChatException;
}

