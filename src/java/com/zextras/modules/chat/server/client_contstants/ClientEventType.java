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

package com.zextras.modules.chat.server.client_contstants;

/**
 * This class contains the event type constants already defined in ZxChatEvent.js
 */
public class ClientEventType
{
  public static final int MESSAGE               = 1;
  public static final int FRIEND_REQUEST        = 2;
  public static final int USER_STATUSES         = 3;
  public static final int CONTACT_INFORMATION   = 4;
  public static final int REQUIRED_REGISTRATION = 5;
  public static final int BROADCAST             = 6;
  public static final int BUDDY_LIST            = 7;
  public static final int TIMEOUT               = 8;
  public static final int WRITING               = 9;
  public static final int MESSAGE_ACK           = 10;
  public static final int FRIEND_BACK_ADDED     = 11;
  public static final int TYPE_CLIENT_UPDATE    = 12;
  public static final int TYPE_SHUTDOWN         = 13;
  public static final int ERROR                 = 14;

  public static final int RTC_SESSION_DESCRIPTION = 27;
  public static final int RTC_ICE_CANDIDATE       = 28;

  public static final int JINGLE                = 29;
}
