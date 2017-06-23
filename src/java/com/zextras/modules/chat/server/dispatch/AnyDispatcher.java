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

package com.zextras.modules.chat.server.dispatch;

import com.zextras.modules.chat.server.events.EventRouter;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;

public class AnyDispatcher implements Dispatcher
{
  private final EventRouter mEventRouter;

  public AnyDispatcher(EventRouter sessionManager)
  {
    mEventRouter = sessionManager;
  }

  @Override
  public void dispatch(Event event) throws ChatException, ChatDbException
  {
    //mEventRouter.deliverToAnyone(event);
  }
}
