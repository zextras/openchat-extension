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

package com.zextras.modules.chat.server.events;

import com.zextras.modules.chat.server.address.NoneAddress;
import com.zextras.modules.chat.server.Target;

public class EventXmppDiscovery extends Event
{
  public String getDomainName()
  {
    return mDomainName;
  }

  private final String mDomainName;

  public boolean isDomainQuery()
  {
    return mDomainQuery;
  }

  private final boolean mDomainQuery;


  /*
    constructor for domain query
  */
  public EventXmppDiscovery(String domainName, EventId eventId, boolean domainQuery)
  {
    super(eventId, new NoneAddress(), new Target());
    mDomainName = domainName;
    mDomainQuery = domainQuery;
  }

  @Override
  public <T> T interpret(EventInterpreter<T> interpreter)
  {
    return interpreter.interpret(this);
  }
}
