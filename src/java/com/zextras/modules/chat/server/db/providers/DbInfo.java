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

package com.zextras.modules.chat.server.db.providers;

public class DbInfo {
  private static final int DB_NOT_INITIALIZED = 0;
  public int mVersion;

  public DbInfo() {
    mVersion = DB_NOT_INITIALIZED;
  }

  public DbInfo(int version) {
    mVersion = version;
  }

  public int getVersion() {
    return mVersion;
  }
}
