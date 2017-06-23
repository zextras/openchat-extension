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

package com.zextras.modules.chat.server;

import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.destinations.EventSenderFactory;
import com.zextras.modules.chat.server.events.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DestinationQueueImpl implements DestinationQueue
{
  private final String mHost;
  private final QueuedEventFactory mQueuedEventFactory;
  private List<QueuedEvent> mQueue;
  private ReentrantLock mLock;
  private Condition mEmptyQueue;
  private EventSender mEventSender;

  public DestinationQueueImpl(
    String host,
    EventSenderFactory eventSenderFactory,
    QueuedEventFactory queuedEventFactory
  )
  {
    mHost = host;
    mQueuedEventFactory = queuedEventFactory;
    mQueue = new ArrayList<QueuedEvent>();
    mLock = new ReentrantLock();
    mEmptyQueue = mLock.newCondition();
    mEventSender = eventSenderFactory.createEventSender(this);
  }

  @Override
  public void stop() throws InterruptedException
  {
    mEventSender.stop();
  }

  @Override
  public void start()
  {
    mEventSender.start();
  }

  @Override
  public void addEvent(Event event, SpecificAddress recipient)
  {
    mLock.lock();
    try
    {
      mQueue.add(mQueuedEventFactory.createQueuedEvent(event, recipient));
      mEmptyQueue.signal();
    }
    finally
    {
      mLock.unlock();
    }
  }

  @Override
  public void addEvent(QueuedEvent queuedEvent)
  {
    mLock.lock();
    try
    {
      mQueue.add(queuedEvent);
      mEmptyQueue.signal();
    }
    finally
    {
      mLock.unlock();
    }
  }

  @Override
  public QueuedEvent getQueuedEvent() throws InterruptedException
  {
    mLock.lock();
    try
    {
      QueuedEvent event;
      while (getQueueSize() < 1)
      {
        mEmptyQueue.await();
      }
      event = mQueue.remove(0);
      return event;
    }
    finally
    {
      mLock.unlock();
    }
  }

  @Override
  public String getHost()
  {
    return mHost;
  }

  @Override
  public int getQueueSize()
  {
    return mQueue.size();
  }
}
