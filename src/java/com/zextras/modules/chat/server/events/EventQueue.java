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

import com.zextras.modules.chat.server.exceptions.EmptyQueueException;
import com.zextras.modules.chat.server.listener.EventQueueListener;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EventQueue
{
  private final Queue<Event> mEventQueue;
  private final Lock mLock = new ReentrantLock();
  private EventQueueListener mEventQueueListener;
  private int mTotalSynchronizedEventsAmount = 0;

  public EventQueue()
  {
    mEventQueue = new ConcurrentLinkedQueue<Event>();
    mEventQueueListener = null;
  }

  public List<Event> popAllEvents(int howMany)
  {
    int counter = howMany;
    final List<Event> eventList = new ArrayList<Event>(mEventQueue.size());

    final EventQueueListener eventQueueListener;
    mLock.lock();
    try
    {
      eventQueueListener = getEventQueueListener();
      while (counter > 0)
      {
        Event event = poll();

        if (event != null)
        {
          eventList.add(event);
          counter -= 1;
        }
        else
        {
          break;
        }
      }
    }
    finally
    {
      mLock.unlock();
    }

    if (eventQueueListener != null)
    {
      for (Event event : eventList)
      {
        eventQueueListener.onEventPopped(this, event);
      }
    }

    return eventList;
  }

  public List<Event> popAllEvents()
  {
    return popAllEvents(Integer.MAX_VALUE);
  }

  public void queueEvent(Event event)
  {
    mEventQueue.add(event);

    final EventQueueListener eventQueueListener = getEventQueueListener();
    if (eventQueueListener != null) {
      eventQueueListener.onEventQueued(this,event);
      eventQueueListener.onEventQueued(this);
    }
  }

  public @Nullable Event poll()
  {
    return mEventQueue.poll();
  }

  public Event popEvent()
    throws EmptyQueueException
  {
    Event event = mEventQueue.poll();

    if(event == null) {
      throw new EmptyQueueException("");
    }

    final EventQueueListener eventQueueListener = getEventQueueListener();
    if (eventQueueListener != null) {
      eventQueueListener.onEventPopped(this,event);
    }

    return event;
  }

  private EventQueueListener getEventQueueListener()
  {
    mLock.lock();
    try
    {
      return mEventQueueListener;
    }
    finally
    {
      mLock.unlock();
    }
  }

  public void flush()
  {
    final EventQueueListener eventListener = getEventQueueListener();
    if (eventListener != null) {
      eventListener.onQueueFlushed(this);
      removeListener();
    }
  }

  public boolean hasEvent()
  {
    return !mEventQueue.isEmpty();
  }

  public int size() {
    return mEventQueue.size();
  }

  public void addListener(EventQueueListener eventListener)
  {
    mLock.lock();
    try
    {
      if (mEventQueueListener != null) {
        throw new IllegalStateException("EventQueue cannot have more than one EventListener");
      }
      mEventQueueListener = eventListener;
      mEventQueueListener.onAttached(this);
    }
    finally
    {
      mLock.unlock();
    }

    if (hasEvent()) {
      eventListener.onEventQueued(this);
    }
  }

  @SuppressWarnings("ObjectEquality")
  public EventQueueListener removeListenerIfEqual(EventQueueListener listener)
  {
    mLock.lock();
    try
    {
      if( mEventQueueListener == listener )
      {
        mEventQueueListener = null;
      }
      return listener;
    }
    finally
    {
      mLock.unlock();
    }
  }

  public EventQueueListener removeListener()
  {
    mLock.lock();
    try
    {
      EventQueueListener eventListener = mEventQueueListener;
      mEventQueueListener = null;
      return eventListener;
    }
    finally
    {
      mLock.unlock();
    }
  }

  public void removeEvent(EventId eventIdToRemove)
  {
    final EventQueueListener eventQueueListener = getEventQueueListener();

    Iterator<Event> eventIterator = mEventQueue.iterator();
    while (eventIterator.hasNext())
    {
      Event event = eventIterator.next();
      if (event.getId().equals(eventIdToRemove))
      {
        eventIterator.remove();
        if( eventQueueListener != null ) {
          eventQueueListener.onEventRemoved(this,event);
        }
      }
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder().append("EventQueue: {");
    Iterator<Event> eventIterator = mEventQueue.iterator();
    while (eventIterator.hasNext()) {
      Event event = eventIterator.next();
      sb.append(event.toString()).append(", ");
    }
    sb.append("}");
    return sb.toString();
  }

  public List<Event> peekAllEvents(int howMany)
  {
    int counter = howMany;
    final List<Event> eventList = new ArrayList<Event>(mEventQueue.size());

    Iterator<Event> iterator = mEventQueue.iterator();
    while (counter > 0)
    {
      if (iterator.hasNext())
      {
        Event event = iterator.next();
        eventList.add(event);
        counter -= 1;
      }
      else
      {
        break;
      }
    }
    return eventList;
  }

  public List<Event> peekAllEvents()
  {
    return new ArrayList<Event>(mEventQueue);
  }

  @SuppressWarnings("LockAcquiredButNotSafelyReleased")
  public void lock()
  {
    mLock.lock();
  }

  public void unlock()
  {
    mLock.unlock();
  }

  public void replaceListener(EventQueueListener eventListener)
  {
    EventQueueListener removed;
    mLock.lock();
    try
    {
      removed = mEventQueueListener;
      mEventQueueListener = eventListener;
      mEventQueueListener.onAttached(this);
    }
    finally
    {
      mLock.unlock();
    }

    if( removed != null ) {
      removed.onDetached(this);
    }

    if( hasEvent() ) {
      eventListener.onEventQueued(this);
    }
  }


  public void popSuccessfullySentEvents(int mSuccessfullySentEvents)
  {
    // With (mSuccessfullySentEvents == -1) it means that current client with this session doesn't support synchronization
    // In this case mSynchronizedEventsAmount stores the previous queue size instead of the previous total synched events
    if (mSuccessfullySentEvents == -1)
    {
      popAllEvents(mTotalSynchronizedEventsAmount);
      mTotalSynchronizedEventsAmount = this.size();
    }
    else
    {
      popAllEvents(mSuccessfullySentEvents - mTotalSynchronizedEventsAmount);
      mTotalSynchronizedEventsAmount = mSuccessfullySentEvents;
    }
  }
}
