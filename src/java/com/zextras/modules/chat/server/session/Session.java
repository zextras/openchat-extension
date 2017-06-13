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

package com.zextras.modules.chat.server.session;

import org.openzal.zal.lib.Filter;
import com.zextras.modules.chat.server.events.EventDestination;
import com.zextras.modules.chat.server.events.EventQueue;
import com.zextras.modules.chat.server.User;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.address.SpecificAddressFromSession;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.status.Status;

public interface Session extends Comparable<SessionUUID>, EventDestination
{
  EventQueue getEventQueue();

  void renew(long newExpireTime);

  User getUser();

  boolean isExpired( long now );

  void setMainAddress(SpecificAddressFromSession address);

  void setExposedAddress(SpecificAddressFromSession address);

  SpecificAddress getMainAddress();

  SpecificAddress getExposedAddress();

  SessionUUID getId();

  void expire();

  @Override
  int compareTo(SessionUUID uid);

  @Override
  String toString();

  @Override
  int hashCode();

  Filter<Event> getOutFilter();

  void setLastStatus(Status status);
  Status getLastStatus();
  void refuseInputEvents();
  void acceptInputEvents();
}
