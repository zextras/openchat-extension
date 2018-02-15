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

package com.zextras.modules.chat.server.session;

import com.zextras.modules.chat.server.events.EventQueue;
import com.zextras.modules.chat.server.User;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.address.SpecificAddressFromSession;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.filters.EventFilter;
import com.zextras.modules.chat.server.status.FixedStatus;
import com.zextras.modules.chat.server.status.Status;

public abstract class BaseSession implements Session
{
  private final SessionUUID mId;
  private final EventQueue mEventQueue;
  private SpecificAddress mMainAddress;
  private SpecificAddress mExposedAddress;
  private final User mUser;
  private Status mLastStatus = FixedStatus.Available;


  public BaseSession(
    SessionUUID id,
    EventQueue eventQueue,
    User user,
    SpecificAddress address
  )
  {
    this(id, eventQueue, user, address, address);
  }

  private BaseSession(
    SessionUUID id,
    EventQueue eventQueue,
    User user,
    SpecificAddress mainAddress,
    SpecificAddress exposedAddress
  )
  {
    mId = id;
    mEventQueue = eventQueue;
    mMainAddress = new SpecificAddressFromSession(mainAddress.toString(),mainAddress.resource(),id);
    mExposedAddress = new SpecificAddressFromSession(exposedAddress.toString(),exposedAddress.resource(),id);
    mUser = user;
  }

  @Override
  public EventQueue getEventQueue()
  {
    return mEventQueue;
  }

  @Override
  public User getUser()
  {
    return mUser;
  }

  @Override
  public void setMainAddress(SpecificAddressFromSession address) {
    mMainAddress = address;
  }

  @Override
  public void setExposedAddress(SpecificAddressFromSession address) {
    mExposedAddress = address;
  }

  @Override
  public SpecificAddress getMainAddress()
  {
    return mMainAddress;
  }

  @Override
  public SpecificAddress getExposedAddress()
  {
    return mExposedAddress;
  }

  @Override
  final public SessionUUID getId()
  {
    return mId;
  }

  @Override
  public int compareTo(SessionUUID uid)
  {
    return mId.compareTo(uid);
  }

  @Override
  public String toString()
  {
    final StringBuilder sb = new StringBuilder();

    sb.append('{');
    sb.append("id=").append(mId.toString()).append(',');
    sb.append("mainAddress=").append(mMainAddress.toString());
    sb.append('}');

    return sb.toString();
  }

  public abstract EventFilter getEventFilter();
  public abstract void refuseInputEvents();
  public abstract void acceptInputEvents();

  public boolean deliverEvent(Event event, SpecificAddress address)
  {
    if( event.getSender().isFromSession(getId()) ) {
      return false;
    }

    try
    {
      if( getEventFilter().isFiltered(event, address, this) ){
        return false;
      }
    }
    catch (ChatException e)
    {
      throw new RuntimeException(e);
    }

    mEventQueue.queueEvent(event);
    return true;
  }

  @Override
  public void expire() {
    mEventQueue.flush();
  }

  @Override
  public int hashCode()
  {
    return mId.hashCode();
  }

  @Override
  public void setLastStatus(Status status)
  {
    mLastStatus = status;
  }

  public Status getLastStatus()
  {
    return mLastStatus;
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

    BaseSession that = (BaseSession) o;

    if (!mEventQueue.equals(that.mEventQueue))
    {
      return false;
    }
    if (!mExposedAddress.equals(that.mExposedAddress))
    {
      return false;
    }
    if (!mId.equals(that.mId))
    {
      return false;
    }
    if (!mLastStatus.equals(that.mLastStatus))
    {
      return false;
    }
    if (!mMainAddress.equals(that.mMainAddress))
    {
      return false;
    }
    if (!mUser.equals(that.mUser))
    {
      return false;
    }

    return true;
  }

  @Override
  public boolean isSoap()
  {
    return false;
  }
}
