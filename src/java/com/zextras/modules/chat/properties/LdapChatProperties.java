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

package com.zextras.modules.chat.properties;

import com.google.inject.Inject;
import org.openzal.zal.Account;
import org.openzal.zal.DistributionList;
import org.openzal.zal.Domain;
import org.openzal.zal.Provisioning;
import org.openzal.zal.Server;

public class LdapChatProperties implements ChatProperties
{
  private final Provisioning mProvisioning;
  private final Server       mLocalServer;

  @Inject
  public LdapChatProperties(Provisioning provisioning)
  {
    mProvisioning = provisioning;
    mLocalServer = provisioning.getLocalServer();
  }

  @Override
  public boolean isSilentErrorReportingEnabled()
  {
    return false;
  }

  @Override
  public boolean isChatServiceEnabled()
  {
    String value = mLocalServer.getAttr("zimbraChatServiceEnabled", "true");
    return "true".equalsIgnoreCase(value);
  }

  @Override
  public boolean isChatHistoryEnabled(String accountName)
  {
    Account account = mProvisioning.getAccountByName(accountName);

    if (account != null)
    {
      String value = account.getAttr("zimbraChatHistoryEnabled", "true");
      return "true".equalsIgnoreCase(value);
    }

    return false;
  }

  @Override
  public boolean isChatConversationAuditEnabled()
  {
    return false;
  }

  @Override
  public boolean isChatConversationAuditEnabled(String domainName)
  {
    Domain domain = mProvisioning.getDomainByName(domainName);

    if (domain != null)
    {
      String value = domain.getAttr("zimbraChatConversationAuditEnabled", "false");
      return "true".equalsIgnoreCase(value);
    }

    return false;
  }

  @Override
  public boolean isChatXmppSslPortEnabled()
  {
    String value = mLocalServer.getAttr("zimbraChatXmppSslPortEnabled", "false");
    return "true".equalsIgnoreCase(value);
  }

  @Override
  public boolean allowUnencryptedPassword()
  {
    String value = mLocalServer.getAttr("zimbraChatAllowUnencryptedPassword", "false");
    return "true".equalsIgnoreCase(value);
  }

  @Override
  public int getChatXmppPort(String serverName)
  {
    int port = ChatProperties.DEFAULT_PLAIN_TLS_PORT;
    Server server;
    if (mLocalServer.getName().equalsIgnoreCase(serverName))
    {
      server = mLocalServer;
    }
    else
    {
      server = mProvisioning.getServerByName(serverName);
    }
    if (server != null)
    {
      port = Integer.valueOf(
        server.getAttr("zimbraChatXmppPort", String.valueOf(ChatProperties.DEFAULT_PLAIN_TLS_PORT))
      );
    }
    return port;
  }

  @Override
  public int getChatXmppSslPort(String serverName)
  {
    int port = ChatProperties.DEFAULT_OLD_SSL_PORT;
    Server server;
    if (mLocalServer.getName().equalsIgnoreCase(serverName))
    {
      server = mLocalServer;
    }
    else
    {
      server = mProvisioning.getServerByName(serverName);
    }
    if (server != null)
    {
      port = Integer.valueOf(
        server.getAttr("zimbraChatXmppSslPort", String.valueOf(ChatProperties.DEFAULT_OLD_SSL_PORT))
      );
    }
    return port;
  }

  @Override
  public boolean chatAllowDlMemberAddAsFriend(DistributionList distributionList)
  {
    String value = distributionList.getAttr("zimbraChatAllowDlMemberAddAsFriend", "false");
    return "true".equalsIgnoreCase(value);
  }

  @Override
  public String getProductName()
  {
    return "OpenChat";
  }
}
