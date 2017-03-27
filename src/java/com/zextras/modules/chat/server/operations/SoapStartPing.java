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

import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventQueue;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.exceptions.NoSuchSessionException;
import com.zextras.modules.chat.server.exceptions.NoSuchSessionInPingException;
import com.zextras.modules.chat.server.listener.PingEventQueueListener;
import com.zextras.modules.chat.server.session.SessionManager;
import com.zextras.modules.chat.server.session.SessionUUID;
import com.zextras.modules.chat.server.soap.SoapSession;
import com.zextras.modules.chat.server.soap.encoders.SoapEncoderFactory;
import com.zextras.modules.chat.server.soap.encoders.SoapEncoderFactoryImpl;
import org.openzal.zal.soap.SoapResponse;

import java.util.Collections;
import java.util.List;


public class SoapStartPing implements ChatOperation
{
  private final SoapResponse           mSoapResponse;
  private final SoapEncoderFactory     mSoapEncoderFactory;
  private final SessionUUID            mSessionId;
  private final PingEventQueueListener mQueueListener;
  private final SpecificAddress        mSenderAddress;

  public SoapStartPing(
    SoapResponse soapResponse,
    SoapEncoderFactory soapEncoderFactory,
    SessionUUID sessionId,
    PingEventQueueListener queueListener,
    SpecificAddress senderAddress
  )
  {
    mSoapResponse = soapResponse;
    mSoapEncoderFactory = soapEncoderFactory;
    mSessionId = sessionId;
    mQueueListener = queueListener;
    mSenderAddress = senderAddress;
  }

  @Override
  public List<Event> exec(SessionManager sessionManager, UserProvider userProvider)
    throws ChatException, ChatDbException
  {
    SoapSession session;
    try
    {
      session = (SoapSession) sessionManager.getSessionById(getSessionId());
    }
    catch (NoSuchSessionException ex)
    {
      ChatLog.log.info("no such session for " + mSenderAddress.toString() + ", requesting registration");
      throw new NoSuchSessionInPingException(getSessionId());
    }

    session.renew(SoapSession.EXPIRE_TIME_IN_MILLIS);

    EventQueue eventQueue = session.getEventQueue();
    eventQueue.lock();
    try
    {
      eventQueue.replaceListener(mQueueListener);

      if (mQueueListener.alreadyReplied())
      {
        mQueueListener.encodeEvents(mSoapResponse, mSoapEncoderFactory);
      }
      else
      {
        long timeoutInMs = 27L * 1000L;
        mQueueListener.suspendContinuation(this, timeoutInMs);
      }
    }
    finally
    {
      eventQueue.unlock();
    }

    return Collections.emptyList();
  }

  public SessionUUID getSessionId()
  {
    return mSessionId;
  }

  public PingEventQueueListener getQueueListener()
  {
    return mQueueListener;
  }
}
