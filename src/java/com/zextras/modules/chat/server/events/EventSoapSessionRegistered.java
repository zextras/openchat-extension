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

import com.zextras.modules.chat.server.session.SessionUUID;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.SpecificAddress;
import org.openzal.zal.lib.Version;

public class EventSoapSessionRegistered extends Event
{
  private final SessionUUID     mSessionId;
  private final Version         mClientVersion;
  private final boolean         mSilentErrorReportingEnabled;
  private final boolean         mIsHistoryEnabled;
  private final SpecificAddress mRoomServiceAddress;

  public EventSoapSessionRegistered(
    SpecificAddress senderAddress,
    SessionUUID sessionId,
    Version clientVersion,
    boolean silentErrorReportingEnabled,
    boolean isHistoryEnabled,
    SpecificAddress roomServiceAddress
  )
  {
    super(senderAddress, new Target());
    mSessionId = sessionId;
    mClientVersion = clientVersion;
    mSilentErrorReportingEnabled = silentErrorReportingEnabled;
    mIsHistoryEnabled = isHistoryEnabled;
    mRoomServiceAddress = roomServiceAddress;
  }

  public SessionUUID getSessionId()
  {
    return mSessionId;
  }

  public SpecificAddress getRoomServiceAddress()
  {
    return mRoomServiceAddress;
  }

  public Version getClientVersion()
  {
    return mClientVersion;
  }

  public boolean isHistoryEnabled()
  {
    return mIsHistoryEnabled;
  }

  public boolean isSilentErrorReportingEnabled()
  {
    return mSilentErrorReportingEnabled;
  }

  @Override
  public <T> T interpret(EventInterpreter<T> interpreter)
  {
    return interpreter.interpret(this);
  }
}
