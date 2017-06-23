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

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.zextras.lib.log.ChatLog;
import com.zextras.lib.log.CurrentLogContext;
import com.zextras.modules.chat.server.soap.encoders.SoapEncoderFactory;
import com.zextras.modules.chat.server.soap.encoders.SoapEncoderFactoryImpl;
import org.openzal.zal.Continuation;
import org.openzal.zal.Account;
import org.openzal.zal.soap.SoapResponse;
import org.openzal.zal.soap.ZimbraContext;

public class SoapHandlerCreator
{
  private final SoapResponse                     mResponse;
  private final Account                          mAccount;
  private final SoapEncoderFactory               mSoapEncoderFactory;
  private final ZimbraContext                    mZimbraContext;
  //private final ZxChatZimlet                     mChatZimlet;
  private final InitialSoapRequestHandlerFactory mInitialSoapRequestHandlerFactory;

  @Inject
  public SoapHandlerCreator(
    SoapEncoderFactory soapEncoderFactory,
    InitialSoapRequestHandlerFactory initialSoapRequestHandlerFactory,
    //ZxChatZimlet chatZimlet,
    @Assisted Account account,
    @Assisted SoapResponse soapResponse,
    @Assisted ZimbraContext zimbraSoapContext
  )
  {
    mResponse = soapResponse;
    mAccount = account;
    mSoapEncoderFactory = soapEncoderFactory;
    mZimbraContext = zimbraSoapContext;
    mInitialSoapRequestHandlerFactory = initialSoapRequestHandlerFactory;
    //mChatZimlet = chatZimlet;
  }

  public ChatSoapRequestHandler getAppropriateHandler()
  {
    if(mAccount == null)
    {
      return new NullAccountHandlerSoap(mResponse);
    }

    // Debug feature to check client cookie coherence, DO NOT REMOVE!
    String myusername = mZimbraContext.getParameter("myusername", null);
    if (myusername != null && !mAccount.getName().equals(myusername))
    {
      CurrentLogContext.begin().setAccount(mAccount).freeze();
      try
      {
        ChatLog.log.warn(
          "Bad client cookie/status, it's convinced to be " +
            myusername +
            " but it's " +
            mAccount.getName()
        );
        return new NullAccountHandlerSoap(mResponse);
      }
      finally
      {
        CurrentLogContext.end();
      }
    }

    final Continuation continuation = mZimbraContext.getContinuation();
    if(continuation.isInitial() || continuation.getObject() == null)
    {
      return mInitialSoapRequestHandlerFactory.create(
        mAccount,
        mZimbraContext,
        //mChatZimlet,
        mResponse
      );
    }
    else
    {
      return new ContinuationHandlerSoap(
        continuation,
        mResponse,
        mSoapEncoderFactory
      );
    }
  }
}
