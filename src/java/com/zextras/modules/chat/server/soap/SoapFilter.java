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

package com.zextras.modules.chat.server.soap;

import com.google.inject.Inject;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventInterpreterAdapter;
import com.zextras.modules.chat.server.events.EventManager;
import com.zextras.modules.chat.server.events.EventXmppPing;
import com.zextras.modules.chat.server.filters.EventFilter;
import com.zextras.modules.chat.server.session.Session;


public class SoapFilter extends EventInterpreterAdapter<Boolean> implements EventFilter
{
  private final EventManager mEventManager;

  @Inject
  public SoapFilter(EventManager eventManager)
  {
    super(false);
    mEventManager = eventManager;
  }

  @Override
  public boolean isFiltered(Event event, SpecificAddress target, Session session)
  {
    return event.interpret(this);
  }

  public Boolean interpret(EventXmppPing eventXmppPing)
  {
    return true;
  }

}
