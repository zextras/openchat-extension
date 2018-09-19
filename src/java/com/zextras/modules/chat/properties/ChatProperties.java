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

import org.openzal.zal.DistributionList;
import org.openzal.zal.Group;

public interface ChatProperties
{
  int MAX_MESSAGE_SIZE       = 16 * 1024;
  int DEFAULT_PLAIN_TLS_PORT = 5222;
  int DEFAULT_OLD_SSL_PORT   = 5223;

  boolean isSilentErrorReportingEnabled();
  boolean isChatServiceEnabled();
  boolean isChatHistoryEnabled(String accountName);
  boolean isChatConversationAuditEnabled();
  boolean isChatConversationAuditEnabled(String domainName);
  boolean isChatXmppSslPortEnabled();
  boolean allowUnencryptedPassword();
  int getChatXmppPort(String serverName);
  int getChatXmppSslPort(String serverName);
  boolean chatAllowDlMemberAddAsFriend(Group distributionList);
  String getProductName();
}
