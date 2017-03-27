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

package com.zextras.modules.chat.server.history;

import com.zextras.modules.chat.server.address.SpecificAddress;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Participants
{
  private final SpecificAddress mSender;
  private final SpecificAddress mRecipient;

  Participants(SpecificAddress sender, SpecificAddress recipient)
  {
    mSender = sender;
    mRecipient = recipient;
  }

  @Override
  public int hashCode()
  {
    return new HashCodeBuilder(17, 31)
      .append(mSender)
      .append(mRecipient)
      .toHashCode();
  }

  @Override
  public boolean equals(Object other)
  {
    if (this == other)
    {
      return true;
    }
    if (!(other instanceof Participants))
    {
      return false;
    }
    Participants otherParticipant = (Participants) other;
    return otherParticipant.getSender().equals(mSender) && otherParticipant.getRecipient().equals(mRecipient);
  }

  public SpecificAddress getRecipient()
  {
    return mRecipient;
  }

  public SpecificAddress getSender()
  {
    return mSender;
  }
}
