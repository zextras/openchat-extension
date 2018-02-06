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

public class VolatileStatus extends StatusAdapter implements Status
{
  private final StatusType            mType;
  private final String                mText;
  private final long                  mValidSince;
  private final List<SpecificAddress> mMeetings;

  public VolatileStatus(
    StatusType type,
    String text,
    long validSinceTimestamp,
    List<SpecificAddress> meetings
  )
  {
    mType = type;
    mText = text;
    mValidSince = validSinceTimestamp;
    mMeetings = meetings;
  }

  public VolatileStatus(StatusType type, String statusText)
  {
    mType = type;
    mText = statusText;
    mValidSince = 0L;
    mMeetings = Collections.emptyList();
  }

  @Override
  public long validSince()
  {
    return mValidSince;
  }

  @Override
  public List<SpecificAddress> meetings()
  {
    return mMeetings;
  }

  @Override
  public Status onlyMeeting(SpecificAddress address)
  {
    if( mMeetings.isEmpty() )
    {
      return this;
    }
    else
    {
      List<SpecificAddress> newMeetings;
      if (mMeetings.contains(address))
      {
        newMeetings = Collections.singletonList(address);
      }
      else
      {
        newMeetings = Collections.emptyList();
      }

      return new VolatileStatus(
        mType,
        mText,
        mValidSince,
        newMeetings
      );
    }
  }

  @Override
  public Status withoutMeetings()
  {
    if( mMeetings.isEmpty() )
    {
      return this;
    }
    else
    {
      return new VolatileStatus(
        mType,
        mText,
        mValidSince,
        Collections.<SpecificAddress>emptyList()
      );
    }
  }

  @Override
  public StatusId getId()
  {
    return new StatusId(mType.toByte());
  }

  @Override
  public String getText()
  {
    return mText;
  }

  @Override
  public StatusType getType()
  {
    return mType;
  }
}
