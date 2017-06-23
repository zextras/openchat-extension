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

package com.zextras.modules.chat.server.soap;

import com.zextras.modules.chat.server.operations.SoapStartPing;
import com.zextras.modules.chat.server.soap.encoders.SoapEncoderFactory;
import com.zextras.modules.chat.server.soap.encoders.SoapEncoderFactoryImpl;
import com.zextras.modules.chat.server.listener.PingEventQueueListener;
import org.openzal.zal.Continuation;
import org.openzal.zal.soap.SoapResponse;

public class ContinuationHandlerSoap implements ChatSoapRequestHandler
{
  private final Continuation       mContinuation;
  private final SoapEncoderFactory mSoapEncoderFactory;
  private final SoapResponse       mResponse;

  public ContinuationHandlerSoap(
    Continuation continuation,
    SoapResponse soapResponse,
    SoapEncoderFactory soapEncoderFactory
  )
  {
    mContinuation = continuation;
    mResponse = soapResponse;
    mSoapEncoderFactory = soapEncoderFactory;
  }

  @Override
  public void handleRequest()
  {
    SoapStartPing startPing = (SoapStartPing) mContinuation.getObject();
    PingEventQueueListener queueListener = startPing.getQueueListener();
    queueListener.encodeEvents(mResponse, mSoapEncoderFactory);
  }
}
