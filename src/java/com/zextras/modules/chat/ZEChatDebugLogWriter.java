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

package com.zextras.modules.chat;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zextras.lib.log.CurrentLogContext;
import com.zextras.lib.log.SeverityLevel;
import com.zextras.lib.log.writers.ZEDailyLogWriter;
import com.zextras.lib.switches.Service;
import com.zextras.modules.chat.properties.ChatProperties;
import org.openzal.zal.Account;
import org.openzal.zal.Provisioning;


@Singleton
public class ZEChatDebugLogWriter extends ZEDailyLogWriter implements Service
{
  private final static String sLogFilePath = "/opt/zimbra/log/chat.log";
  private final ChatProperties mChatProperties;
  private final Provisioning mProvisioning;

  @Inject
  public ZEChatDebugLogWriter(
    ChatProperties chatProperties,
    Provisioning provisioning
  )
  {
    super(sLogFilePath, ZEChatDebugLogWriter.class, provisioning);
    mChatProperties = chatProperties;
    mProvisioning = provisioning;
  }

  public int getPriority()
  {
    return 110;
  }

  public String getName()
  {
    return "ChatLogger";
  }

  public String getDescription()
  {
    return "Chat Logger";
  }

  @Override
  public void flush() {}

  @Override
  public void start() throws ServiceStartException
  {
    setEnabled(true);
  }

  @Override
  public void stop()
  {
    unregister();
    setEnabled(false);
  }

  @Override
  public boolean isLogged( SeverityLevel level )
  {
    String domainName = "";
    String accountName = CurrentLogContext.current().getAccountName();
    if (!accountName.isEmpty())
    {
      Account account = mProvisioning.getAccountByName(accountName);
      if (account != null)
      {
        domainName = account.getDomainName();
      }
    }
    return ( mChatProperties.isChatConversationAuditEnabled() ||
      mChatProperties.isChatConversationAuditEnabled(domainName) ) && super.isLogged(level);
  }
}
