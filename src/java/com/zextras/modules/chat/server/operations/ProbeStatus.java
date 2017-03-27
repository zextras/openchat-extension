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

package com.zextras.modules.chat.server.operations;

import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.session.SessionManager;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventStatusProbe;
import com.zextras.modules.chat.server.exceptions.ChatException;

import java.util.Arrays;
import java.util.List;

public class ProbeStatus implements ChatOperation
{
  private final SpecificAddress mSender;
  private final SpecificAddress mTarget;

  public ProbeStatus(SpecificAddress sender, SpecificAddress target)
  {
    mSender = sender.withoutSession();
    mTarget = target.withoutSession();
  }

  @Override
  public List<Event> exec(SessionManager sessionManager, UserProvider userProvider) throws ChatException, ChatDbException
  {
    Event event = new EventStatusProbe(
      mSender,
      new Target(mTarget)
    );
    return Arrays.asList(event);
  }
}
