/*
 * ZAL - The abstraction layer for Zimbra.
 * Copyright (C) 2017 ZeXtras S.r.l.
 *
 * This file is part of ZAL.
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
 * You should have received a copy of the GNU General Public License
 * along with ZAL. If not, see <http://www.gnu.org/licenses/>.
 */

package com.zextras.modules.chat.server.db.mappers;

import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.exceptions.ChatDbException;

import java.util.Collection;

/**
 * Event mapper interface.
 */
public interface BaseEventMapper<T extends Event> {
  public abstract Collection<T> get(int userId) throws ChatDbException;
  public abstract int insert(int userId, Event event) throws ChatDbException;
  public abstract int deleteAll(int userId) throws ChatDbException;
  public abstract int delete(int userId, String eventId) throws ChatDbException;
}
