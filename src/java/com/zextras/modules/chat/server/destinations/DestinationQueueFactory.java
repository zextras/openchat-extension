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

package com.zextras.modules.chat.server.destinations;

import com.google.inject.Inject;
import com.zextras.modules.chat.server.DestinationQueue;
import com.zextras.modules.chat.server.DestinationQueueImpl;
import com.zextras.modules.chat.server.QueuedEventFactory;

public class DestinationQueueFactory
{
  private final EventSenderFactory mEventSenderFactory;
  private final QueuedEventFactory mQueuedEventFactory;

  @Inject
  public DestinationQueueFactory(EventSenderFactory eventSenderFactory, QueuedEventFactory queuedEventFactory)
  {
    mEventSenderFactory = eventSenderFactory;
    mQueuedEventFactory = queuedEventFactory;
  }

  public DestinationQueue createQueue(String address)
  {
    return new DestinationQueueImpl(address, mEventSenderFactory, mQueuedEventFactory);
  }
}
