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

package com.zextras.modules.chat.server.listener;

import java.lang.Runnable;
import com.zextras.lib.activities.ActivityManager;
import com.zextras.lib.activities.ActivityTimer;
import com.zextras.modules.chat.server.encoding.Encoder;
import com.zextras.modules.chat.server.events.EventInterpreter;
import com.zextras.modules.chat.server.events.EventQueue;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.response.ChatSoapResponse;
import com.zextras.modules.chat.server.soap.SoapEncoder;
import com.zextras.modules.chat.server.events.Event;
import org.openzal.zal.Continuation;
import org.openzal.zal.soap.SoapResponse;

import java.util.concurrent.locks.ReentrantLock;


public class PingEventQueueListener implements EventQueueListener
{
  private final Continuation    mContinuation;
  private final SpecificAddress mAddress;
  private final ReentrantLock   mLock;
  private final ActivityManager mActivityManager;
  private final int mSuccessfullySentEvents;
  private       boolean         mCanBeResumed;
  private       boolean         mAlreadyReplied;
  private       boolean         mDetached;
  private       EventQueue      mEventQueue;
  private       ActivityTimer   mTimeoutTimer;

  public PingEventQueueListener(
    ActivityManager activityManager,
    SpecificAddress address,
    Continuation continuation,
    int successFullySentEvents
  )
  {
    mActivityManager = activityManager;
    mLock = new ReentrantLock();
    mAddress = address;
    mContinuation = continuation;
    mSuccessfullySentEvents = successFullySentEvents;
    mAlreadyReplied = false;
    mDetached = false;
    mCanBeResumed = false;
    mTimeoutTimer = null;
  }

  public boolean alreadyReplied()
  {
    return mAlreadyReplied;
  }

  @Override
  public void onAttached(EventQueue eventQueue)
  {
    mEventQueue = eventQueue;
  }

  @Override
  public void onDetached(EventQueue eventQueue)
  {
    mEventQueue = new EventQueue();
    mDetached = true;
    resumeContinuationAndRemoveListener();
  }

  @Override
  public void onEventQueued(EventQueue eventQueue, Event queuedEvent)
  {
  }

  public void onEventQueued(EventQueue eventQueue)
  {
    mAlreadyReplied = true;
    mEventQueue = eventQueue;
    resumeContinuationAndRemoveListener();
  }

  public void suspendContinuation(
    Object continuationObject,
    long timeoutInMs
  )
  {
    mLock.lock();
    try
    {
      if( mCanBeResumed ) {
        throw new RuntimeException("Cannot suspend already suspended request");
      }

      mContinuation.setObject(continuationObject);
      mTimeoutTimer = mActivityManager.scheduleActivity(
        new Runnable()
        {
          @Override
          public void run()
          {
            mEventQueue.removeListenerIfEqual(PingEventQueueListener.this);
            mEventQueue = new EventQueue();
            resumeContinuationAndRemoveListener();
          }
        },
        timeoutInMs
      );

      mCanBeResumed = true;
      mContinuation.suspend();
    }
    finally
    {
      mLock.unlock();
    }
  }

  private void resumeContinuationAndRemoveListener()
  {
// check needed in case of double ping to avoid
// removal of another valid listener
    mEventQueue.removeListenerIfEqual(this);

    mLock.lock();
    try
    {
      if(mCanBeResumed && mContinuation.isSuspended())
      {
        mTimeoutTimer.cancel();
        mTimeoutTimer = null;
        mActivityManager.scheduleActivity(
          new Runnable()
          {
            @Override
            public void run()
            {
              mContinuation.resume();
            }
          },
          250L
        );
        mCanBeResumed = false;
      }
    }
    finally
    {
      mLock.unlock();
    }
  }

  @Override
  public void onEventPopped(EventQueue eventQueue, Event poppedEvent) {
  }

  @Override
  public void onQueueFlushed(EventQueue eventQueue) {
    mEventQueue.removeListener();
  }

  @Override
  public void onEventRemoved(EventQueue eventQueue, Event event) {
  }

  public void encodeEvents(SoapResponse response, EventInterpreter<Encoder> encoderFactory)
  {
    mLock.lock();
    try
    {
      if (mEventQueue == null)
      {
        throw new RuntimeException("EventQueue is null");
      }

      if (mDetached)
      {
        response.setValue("error", new ConcurrentPingException().toJSON().toString());
      }
      else
      {
        // update session queue throught client successfully received events
        mEventQueue.popSuccessfullySentEvents(mSuccessfullySentEvents);

        ChatSoapResponse chatSoapResponse = new ChatSoapResponse();
        for (Event event : mEventQueue.peekAllEvents(200))
        {
          SoapEncoder encoder = (SoapEncoder) event.interpret(encoderFactory);
          encoder.encode(chatSoapResponse, mAddress);
        }
        chatSoapResponse.encodeInSoapResponse(response);
        mEventQueue.removeListener();
      }
    }
    finally
    {
      mLock.unlock();
    }
  }

  @Override
  public String toString() {
    return "PingEventQueueListener{" +
        ", mAddress=" + mAddress +
        ", mAlreadyReplied=" + mAlreadyReplied +
        ", mEventQueue=" + mEventQueue +
        ", HashCode=" + hashCode() +
        '}';
  }
}
