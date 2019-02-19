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

package com.zextras.modules.chat.server.listener;

import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.encoding.Encoder;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventInterpreter;
import com.zextras.modules.chat.server.events.EventQueue;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.response.ChatSoapResponse;
import com.zextras.modules.chat.server.soap.SoapEncoder;
import org.openzal.zal.soap.SoapResponse;

import java.util.concurrent.locks.ReentrantLock;


public class PingWebSocketQueueListener implements PingQueueListener
{
  private final SpecificAddress mAddress;
  private final ReentrantLock   mLock;
  private final int             mSuccessfullySentEvents;
  private       boolean         mAlreadyReplied;
  private       boolean         mDetached;
  private       EventQueue      mEventQueue;

  public PingWebSocketQueueListener(
    SpecificAddress address,
    int successFullySentEvents
  )
  {
    mLock = new ReentrantLock();
    mAddress = address;
    mSuccessfullySentEvents = successFullySentEvents;
    mAlreadyReplied = false;
    mDetached = false;
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
    // check needed in case of double ping to avoid
    // removal of another valid listener
    mEventQueue.removeListenerIfEqual(this);
  }

  @Override
  public void onEventQueued(EventQueue eventQueue, Event queuedEvent)
  {
  }

  @Override
  public void onEventQueued(EventQueue eventQueue)
  {
    mEventQueue = eventQueue;
    if(mEventQueue.hasNewEvents(mSuccessfullySentEvents))
    {
      mAlreadyReplied = true;
      // check needed in case of double ping to avoid
      // removal of another valid listener
      mEventQueue.removeListenerIfEqual(this);
    }
  }

  @Override
  public void suspendContinuation(
    Object continuationObject,
    long timeoutInMs
  )
  {
    mEventQueue.removeListenerIfEqual(PingWebSocketQueueListener.this);
    mEventQueue = new EventQueue();
  }

  @Override
  public void onEventPopped(EventQueue eventQueue, Event poppedEvent)
  {
  }

  @Override
  public void onQueueFlushed(EventQueue eventQueue)
  {
    mEventQueue.removeListener();
  }

  @Override
  public void onEventRemoved(EventQueue eventQueue, Event event)
  {
  }

  @Override
  public boolean alreadyReplied()
  {
    return mAlreadyReplied;
  }

  public void encodeEvents(SoapResponse response, EventInterpreter<Encoder> encoderFactory)
  {
    mLock.lock();
    try
    {
      if(mEventQueue == null)
      {
        throw new RuntimeException("EventQueue is null");
      }

      if(mDetached)
      {
        response.setValue("error", new ConcurrentPingException().toJSON().toString());
      }
      else
      {
        // update session queue throught client successfully received events
        mEventQueue.popSuccessfullySentEvents(mSuccessfullySentEvents);

        ChatSoapResponse chatSoapResponse = new ChatSoapResponse();
        for(Event event : mEventQueue.peekAllEvents(200))
        {
          try
          {
            SoapEncoder encoder = (SoapEncoder) event.interpret(encoderFactory);
            encoder.encode(chatSoapResponse, mAddress);
          }
          catch(ChatException e)
          {
            throw new RuntimeException(e);
          }
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
  public String toString()
  {
    return "PingWebSocketQueueListener{" +
      ", mAddress=" + mAddress +
      ", mAlreadyReplied=" + mAlreadyReplied +
      ", mEventQueue=" + mEventQueue +
      ", HashCode=" + hashCode() +
      '}';
  }
}
