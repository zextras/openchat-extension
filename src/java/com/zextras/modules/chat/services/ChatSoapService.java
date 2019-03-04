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

package com.zextras.modules.chat.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zextras.lib.switches.Service;
import com.zextras.modules.chat.server.soap.ChatGuestSoapHandler;
import com.zextras.modules.chat.server.soap.ChatSoapHandler;
import org.openzal.zal.soap.QName;
import org.openzal.zal.soap.SoapHandler;
import org.openzal.zal.soap.SoapService;
import org.openzal.zal.soap.SoapServiceManager;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class ChatSoapService implements Service, SoapService
{
  private static final String ServiceName               = "SoapServlet";
  private static final QName  CHAT_CLIENT_REQUEST       = new QName("ZxChatRequest", "urn:zimbraAccount");
  private static final QName  CHAT_CLIENT_GUEST_REQUEST = new QName("ZxChatGuestRequest", "urn:zimbraAccount");

  private final Map<QName, SoapHandler> mHandlerMap;
  private final SoapServiceManager      mSoapServiceManager;

  @Inject
  public ChatSoapService(
    SoapServiceManager soapServiceManager,
    ChatSoapHandler chatSoapHandler,
    ChatGuestSoapHandler chatGuestSoapHandler
  )
  {
    mSoapServiceManager = soapServiceManager;

    mHandlerMap = new HashMap<>();
    mHandlerMap.put(CHAT_CLIENT_REQUEST, chatSoapHandler);
    mHandlerMap.put(CHAT_CLIENT_GUEST_REQUEST, chatGuestSoapHandler);
  }

  @Override
  public void start()
    throws ServiceStartException
  {
    mSoapServiceManager.register(this);
  }

  @Override
  public void stop()
  {
    mSoapServiceManager.unregister(this);
  }

  @Override
  public Map<org.openzal.zal.soap.QName, ? extends SoapHandler> getServices()
  {
    return mHandlerMap;
  }

  @Override
  public String getServiceName()
  {
    return ServiceName;
  }

  @Override
  public boolean isAdminService()
  {
    return false;
  }
}
