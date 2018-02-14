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

package com.zextras.modules.chat.server.destinations;

import com.google.inject.Inject;
import com.zextras.modules.chat.server.DestinationQueue;
import com.zextras.modules.chat.server.DestinationQueueImpl;
import com.zextras.modules.chat.server.QueuedEventFactory;
import org.openzal.zal.lib.Clock;

public class DestinationQueueFactory
{
  private final EventSenderFactory mEventSenderFactory;
  private final QueuedEventFactory mQueuedEventFactory;
  private final Clock mClock;

  @Inject
  public DestinationQueueFactory(
    EventSenderFactory eventSenderFactory,
    QueuedEventFactory queuedEventFactory,
    Clock clock
  )
  {
    mEventSenderFactory = eventSenderFactory;
    mQueuedEventFactory = queuedEventFactory;
    mClock = clock;
  }

  public DestinationQueue createQueue(String address)
  {
    return new DestinationQueueImpl(address, mEventSenderFactory, mQueuedEventFactory, mClock);
  }
}
