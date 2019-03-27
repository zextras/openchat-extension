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

import com.google.inject.Inject;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventRouter;
import com.zextras.modules.chat.server.exceptions.ChatException;

public class AnyServerDispatcher implements Dispatcher
{
  private final EventRouter           mEventRouter;
  private final ServerHostSetProvider mRoomServerHostSetProvider;

  @Inject
  public AnyServerDispatcher(
    EventRouter sessionManager,
    ServerHostSetProvider roomServerHostSetProvider
  )
  {
    mEventRouter = sessionManager;
    mRoomServerHostSetProvider = roomServerHostSetProvider;
  }

  @Override
  public void dispatch(Event event) throws ChatException
  {
    for( SpecificAddress server :  mRoomServerHostSetProvider.getAllServersAddresses() )
    {
      mEventRouter.deliverEvent( server, event );
    }
  }
}
