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

package com.zextras.modules.chat.server.xmpp;

public enum ConnectionType
{
  Client("jabber:client"), Server("jabber:server");

  private String mXmpp;

  ConnectionType(String xmpp)
  {
    mXmpp = xmpp;
  }

  public String toXmpp()
  {
    return mXmpp;
  }

  public static ConnectionType fromXmpp(String str)
  {
    if (Client.toXmpp().equalsIgnoreCase(str)) {
      return Client;
    }
    if (Server.toXmpp().equalsIgnoreCase(str)) {
      return Server;
    }
    throw new IllegalArgumentException("invalid connection type "+str);
  }
}
