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

package com.zextras.modules.chat.server.events;

import com.zextras.modules.chat.server.exceptions.EmptyQueueException;
import com.zextras.modules.chat.server.listener.EventQueueListener;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EventQueue
{
  private final ConcurrentLinkedQueue<Event> mEventQueue;
  private final Lock mListenerLock = new ReentrantLock();
  private       EventQueueListener    mEventQueueListener;
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
    mListenerLock.lock();
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
      mListenerLock.unlock();
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
    Event event = mEventQueue.poll();

    if (event != null)
    {
      final EventQueueListener eventQueueListener = getEventQueueListener();
      if (eventQueueListener != null) {
        eventQueueListener.onEventPopped(this,event);
      }
    }


    return event;
  }

  public Event popEvent()
    throws EmptyQueueException
  {
    Event event = poll();

    if(event == null) {
      throw new EmptyQueueException("");
    }

    return event;
  }

  private EventQueueListener getEventQueueListener()
  {
    mListenerLock.lock();
    try
    {
      return mEventQueueListener;
    }
    finally
    {
      mListenerLock.unlock();
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
    mListenerLock.lock();
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
      mListenerLock.unlock();
    }

    if (hasEvent()) {
      eventListener.onEventQueued(this);
    }
  }

  @SuppressWarnings("ObjectEquality")
  public EventQueueListener removeListenerIfEqual(EventQueueListener listener)
  {
    mListenerLock.lock();
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
      mListenerLock.unlock();
    }
  }

  public EventQueueListener removeListener()
  {
    mListenerLock.lock();
    try
    {
      EventQueueListener eventListener = mEventQueueListener;
      mEventQueueListener = null;
      return eventListener;
    }
    finally
    {
      mListenerLock.unlock();
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
    mListenerLock.lock();
  }

  public void unlock()
  {
    mListenerLock.unlock();
  }

  public void replaceListener(EventQueueListener eventListener)
  {
    EventQueueListener removed;
    mListenerLock.lock();
    try
    {
      removed = mEventQueueListener;
      mEventQueueListener = eventListener;
      mEventQueueListener.onAttached(this);
    }
    finally
    {
      mListenerLock.unlock();
    }

    if( removed != null ) {
      removed.onDetached(this);
    }

    if( hasEvent() ) {
      eventListener.onEventQueued(this);
    }
  }


  public void popSuccessfullySentEvents(int successfullySentEvents)
  {
    // With (mSuccessfullySentEvents == -1) it means that current client with this session doesn't support synchronization
    // In this case mSynchronizedEventsAmount stores the previous queue size instead of the previous total synched events
    if (successfullySentEvents == -1)
    {
      popAllEvents(mTotalSynchronizedEventsAmount);
      mTotalSynchronizedEventsAmount = this.size();
    }
    else
    {
      popAllEvents(successfullySentEvents - mTotalSynchronizedEventsAmount);
      mTotalSynchronizedEventsAmount = successfullySentEvents;
    }
  }

  public boolean hasNewEvents(int successfullySentEvents)
  {
    if (successfullySentEvents == -1)
    {
      return !mEventQueue.isEmpty();
    }
    else
    {
      return mEventQueue.size() - (successfullySentEvents - mTotalSynchronizedEventsAmount) > 0;
    }
  }
}
