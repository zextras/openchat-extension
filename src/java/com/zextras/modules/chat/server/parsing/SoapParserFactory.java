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

package com.zextras.modules.chat.server.parsing;

import com.google.inject.Inject;
import com.zextras.lib.activities.ActivityManager;
import com.zextras.modules.chat.properties.LdapChatProperties;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.soap.SoapSessionFactory;
import com.zextras.modules.chat.server.soap.encoders.SoapEncoderFactory;
import org.openzal.zal.Provisioning;
import org.openzal.zal.soap.SoapResponse;
import org.openzal.zal.soap.ZimbraContext;

public class SoapParserFactory implements ParserFactory
{
  private final Provisioning       mProvisioning;
  private final SoapEncoderFactory mSoapEncoderFactory;
  private final SoapSessionFactory mSoapSessionFactory;
  private final LdapChatProperties mChatProperties;
  private final ActivityManager    mActivityManager;

  @Inject
  public SoapParserFactory(
    Provisioning provisioning,
    SoapEncoderFactory soapEncoderFactory,
    SoapSessionFactory soapSessionFactory,
    LdapChatProperties chatProperties,
    ActivityManager activityManager
  )
  {
    mProvisioning = provisioning;
    mSoapEncoderFactory = soapEncoderFactory;
    mSoapSessionFactory = soapSessionFactory;
    mChatProperties = chatProperties;
    mActivityManager = activityManager;
  }

  @Override
  public Parser create(SpecificAddress senderAddress, ZimbraContext zimbraContext, SoapResponse soapResponse)
  {
    return new SoapParser(
      mProvisioning,
      mSoapEncoderFactory,
      mSoapSessionFactory,
      mChatProperties,
      mActivityManager,
      senderAddress,
      zimbraContext,
      soapResponse
    );
  }
}
