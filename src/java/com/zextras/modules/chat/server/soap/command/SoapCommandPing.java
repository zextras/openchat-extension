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

package com.zextras.modules.chat.server.soap.command;

import com.zextras.lib.activities.ActivityManager;
import com.zextras.modules.chat.server.listener.PingQueueListener;
import com.zextras.modules.chat.server.listener.PingWebSocketQueueListener;
import com.zextras.modules.chat.server.session.SessionUUID;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.operations.SoapStartPing;
import com.zextras.modules.chat.server.exceptions.InvalidParameterException;
import com.zextras.modules.chat.server.exceptions.MissingParameterException;
import com.zextras.modules.chat.server.listener.PingSoapQueueListener;
import com.zextras.modules.chat.server.soap.encoders.SoapEncoderFactory;
import org.openzal.zal.soap.SoapResponse;
import org.openzal.zal.soap.ZimbraContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SoapCommandPing extends SoapCommand
{
  private final SoapResponse       mSoapResponse;
  private final SoapEncoderFactory mSoapEncoderFactory;
  private final ZimbraContext      mZimbraContext;
  private final ActivityManager    mActivityManager;
  private final boolean            mIsWebSocket;

  public SoapCommandPing(
    SoapResponse soapResponse,
    SoapEncoderFactory soapEncoderFactory,
    SpecificAddress senderAddress,
    Map<String, String> parameters,
    ZimbraContext zimbraContext,
    ActivityManager activityManager,
    boolean isWebSocket
  )
  {
    super(
      senderAddress,
      parameters
    );
    mSoapResponse = soapResponse;
    mSoapEncoderFactory = soapEncoderFactory;
    mZimbraContext = zimbraContext;
    mActivityManager = activityManager;
    mIsWebSocket = isWebSocket;
  }

  @Override
  public List<ChatOperation> createOperationList()
    throws MissingParameterException, InvalidParameterException
  {
    if (!mParameterMap.containsKey(SESSION_ID))
    {
      throw new MissingParameterException("Missing parameters to create " + getClass().getName());
    }

    //Retrocompatibility condition
    int successfullySentEvents = Integer.parseInt(mParameterMap.containsKey(SESSION_SUCCESSFULLY_SENT_EVENTS) ? mParameterMap.get(SESSION_SUCCESSFULLY_SENT_EVENTS) : "-1");

    PingQueueListener queueListener;
    if(mIsWebSocket)
    {
      queueListener = new PingWebSocketQueueListener(mSenderAddress, successfullySentEvents);
    }
    else
    {
      queueListener = new PingSoapQueueListener(
        mActivityManager,
        mSenderAddress,
        mZimbraContext.getContinuation(),
        successfullySentEvents
      );
    }

    ChatOperation pingOperation = new SoapStartPing(
      mSoapResponse,
      mSoapEncoderFactory,
      SessionUUID.fromString(mParameterMap.get(SESSION_ID)),
      queueListener,
      mSenderAddress
    );

    return Arrays.<ChatOperation>asList(pingOperation);
  }
}
