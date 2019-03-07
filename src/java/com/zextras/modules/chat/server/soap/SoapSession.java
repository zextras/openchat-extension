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
import com.google.inject.assistedinject.Assisted;
import com.zextras.lib.Optional;
import org.openzal.zal.lib.Clock;
import org.openzal.zal.lib.Filter;
import com.zextras.lib.filters.FilterPassAll;
import com.zextras.modules.chat.server.events.*;
import com.zextras.modules.chat.server.filters.EventFilter;
import com.zextras.modules.chat.server.session.SessionUUID;
import com.zextras.modules.chat.server.User;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.session.BaseSession;
import com.zextras.modules.chat.server.session.SoapEventFilter;
import org.openzal.zal.lib.Version;

public class SoapSession extends BaseSession
{
  public final static long EXPIRE_TIME_IN_MILLIS = 50000L;

  private       long        mExpireTime;
  private final Filter<Event> mOutFilter;
  private final Clock mClock;
  private final EventFilter mEventFilter;
  private final Version mClientVersion;
  private       Version mNotifiedVersion;

  @Inject
  public SoapSession(
    @Assisted SessionUUID id,
    @Assisted EventQueue eventQueue,
    @Assisted User user,
    @Assisted SpecificAddress address,
    @Assisted Version clientVersion,
    @Assisted Filter<Event> outFilter,
    Clock clock,
    SoapEventFilter soapEventFilter
  )
  {
    super(id, eventQueue, user, new SpecificAddress(address.toString(), id.toString()));
    mClientVersion = clientVersion;
    mOutFilter = outFilter;
    mClock = clock;
    mExpireTime = mClock.now() + EXPIRE_TIME_IN_MILLIS;
    mEventFilter = soapEventFilter;
    mNotifiedVersion = new Version(0);
  }

  @Override
  public void renew(long newExpireTime)
  {
    mExpireTime = mClock.now() + newExpireTime;
  }

  @Override
  public boolean isExpired(long now)
  {
    return now > mExpireTime;
  }

  @Override
  public Filter<Event> getOutFilter()
  {
    return mOutFilter;
  }

  @Override
  public EventFilter getEventFilter()
  {
    return mEventFilter;
  }

  @Override
  public void refuseInputEvents()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void acceptInputEvents()
  {
    throw new UnsupportedOperationException();
  }

  public Version getClientVersion()
  {
    return mClientVersion;
  }

  public Version getNotifiedVersion() {
    return mNotifiedVersion;
  }
  public void setNotifiedVersion(Version notifiedVersion) {
    mNotifiedVersion = notifiedVersion;
  }

  @Override
  public boolean isSoap()
  {
    return true;
  }
}
