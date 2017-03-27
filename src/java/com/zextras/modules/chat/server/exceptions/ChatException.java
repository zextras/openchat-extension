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

package com.zextras.modules.chat.server.exceptions;

import com.zextras.lib.Error.ErrorCode;
import com.zextras.lib.Error.ZxError;
import com.zextras.lib.log.SeverityLevel;

import java.util.logging.Level;

public class ChatException extends ZxError
{
  public ChatException(String message)
  {
    super(new ErrorCode()
    {
      @Override
      public String getCodeString()
      {
        return null;
      }

      @Override
      public String getMessage()
      {
        return null;
      }
    });
    setDetail("details", message);
  }

  public ChatException(SeverityLevel severityLevel, ErrorCode code)
  {
    super(severityLevel, code);
  }

  public ChatException(ErrorCode code)
  {
    super(code);
  }
}
