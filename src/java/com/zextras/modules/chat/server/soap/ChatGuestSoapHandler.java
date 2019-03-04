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
import com.google.inject.Singleton;
import com.zextras.lib.Optional;
import com.zextras.lib.log.ChatLog;
import org.openzal.zal.Account;
import org.openzal.zal.ContinuationThrowable;
import org.openzal.zal.Provisioning;
import org.openzal.zal.Utils;
import org.openzal.zal.soap.SoapHandler;
import org.openzal.zal.soap.SoapResponse;
import org.openzal.zal.soap.ZimbraContext;
import org.openzal.zal.soap.ZimbraExceptionContainer;

//zmsoap -t account -m user1@example.com -p assext  ZxChatRequest/action=query_archive ../with=user2@example.com ../session_id=687c6492-9027-416c-8172-2d668154d2d1 | recode html..text
@Singleton
public class ChatGuestSoapHandler implements SoapHandler
{
  private final SoapHandlerCreatorFactory mSoapHandlerCreatorFactory;
  private final Provisioning              mProvisioning;
  //private final GenericReportBuilder      mGenericReportBuilder;
  //private final ReportManager             mReportManager;

  @Inject
  public ChatGuestSoapHandler(
    SoapHandlerCreatorFactory soapHandlerCreatorFactory,
    Provisioning provisioning
    //GenericReportBuilder genericReportBuilder,
    //ReportManager reportManager
  )
  {
    mSoapHandlerCreatorFactory = soapHandlerCreatorFactory;
    mProvisioning = provisioning;
    //mGenericReportBuilder = genericReportBuilder;
    //mReportManager = reportManager;
  }

  public String getLoggerName()
  {
    return "Chat Guest Soap Handler";
  }

  @Override
  public void handleRequest(
    ZimbraContext context,
    SoapResponse soapResponse,
    ZimbraExceptionContainer zimbraExceptionContainer
  )
  {
    try
    {
      final SoapHandlerCreator handlerCreator = mSoapHandlerCreatorFactory.create(
        Optional.<Account>empty(),
        soapResponse,
        context
      );

      handlerCreator.getAppropriateHandler().handleRequest();
    }
    catch( ContinuationThrowable ex )
    {
      throw ex;
    }
    catch (Throwable ex)
    {
      zimbraExceptionContainer.setException(ex);
      ChatLog.log.warn("Internal error:" + Utils.exceptionToString(ex));
      //Report report = mGenericReportBuilder.createReport(ex, ModuleName.CHAT);
      //mReportManager.doReport(report);
    }
  }

  @Override
  public boolean needsAdminAuthentication(ZimbraContext context)
  {
    return false;
  }

  @Override
  public boolean needsAuthentication(ZimbraContext context)
  {
    return false;
  }
}
