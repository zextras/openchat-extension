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

package com.zextras.modules.chat.server.exceptions;

import com.zextras.lib.Error.ErrorCode;
import com.zextras.modules.chat.server.session.SessionUUID;


public class NoSuchSessionException extends ChatException
{
  private final SessionUUID mSessionId;

  public NoSuchSessionException(SessionUUID sessionId)
  {
    super(new ErrorCode()
    {
      public String getCodeString()
      {
        return "NO_SUCH_CHAT_SESSION";
      }

      public String getMessage()
      {
        return "Chat session {session_id} not found";
      }
    });
    setDetail("session_id",sessionId.toString());
    mSessionId = sessionId;
  }

  public SessionUUID getSessionId()
  {
    return mSessionId;
  }
}
