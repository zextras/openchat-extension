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

package com.zextras.modules.chat.server.status;

import com.zextras.modules.chat.server.address.SpecificAddress;

import java.util.Collections;
import java.util.List;

public class FixedStatus extends StatusAdapter implements Status
{
  private final StatusType mType;

  public static final FixedStatus Unknown      = new FixedStatus(StatusType.UNKNOWN);
  public static final FixedStatus Offline      = new FixedStatus(StatusType.OFFLINE);
  public static final FixedStatus Available    = new FixedStatus(StatusType.AVAILABLE);
  public static final FixedStatus Busy         = new FixedStatus(StatusType.BUSY);
  public static final FixedStatus Away         = new FixedStatus(StatusType.AWAY);
  public static final FixedStatus Invisible    = new FixedStatus(StatusType.INVISIBLE);
  public static final FixedStatus NeedResponse = new FixedStatus(StatusType.NEED_RESPONSE);
  public static final FixedStatus Invited      = new FixedStatus(StatusType.INVITED);
  public static final FixedStatus Unreachable  = new FixedStatus(StatusType.UNREACHABLE);
  public static final FixedStatus Unsuscribed  = new FixedStatus(StatusType.UNSUBSCRIBED);

  public FixedStatus(StatusType type)
  {
    mType = type;
  }

  @Override
  public String getText() {
    return "";
  }

  @Override
  public StatusType getType() {
    return mType;
  }

  @Override
  public long validSince()
  {
    return 0L;
  }

  @Override
  public List<SpecificAddress> meetings()
  {
    return Collections.emptyList();
  }

  @Override
  public Status onlyMeeting(SpecificAddress address)
  {
    return this;
  }

  @Override
  public Status withoutMeetings()
  {
    return this;
  }

  @Override
  public StatusId getId() {
    return new StatusId(mType.toByte());
  }

  public static Status fromId(StatusId statusId)
  {
    return new FixedStatus(StatusType.fromByte((byte) statusId.id()));
  }
}
