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
import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.ZEChatDebugLogWriter;
import com.zextras.modules.chat.properties.ChatProperties;
import com.zextras.lib.switches.Service;

@Singleton
public class ChatDebugLogService implements Service
{
  private final ZEChatDebugLogWriter mChatDebugLogWriter;

  @Inject
  public ChatDebugLogService(
    ZEChatDebugLogWriter chatDebugLogWriter
  )
  {
    mChatDebugLogWriter = chatDebugLogWriter;
  }


  @Override
  public void start() throws ServiceStartException {
    mChatDebugLogWriter.setEnabled(true);

    ChatLog.log.addLogWriter(mChatDebugLogWriter);
  }

  @Override
  public void stop() {
    ChatLog.log.removeAllLogWriters(false);
  }
}
