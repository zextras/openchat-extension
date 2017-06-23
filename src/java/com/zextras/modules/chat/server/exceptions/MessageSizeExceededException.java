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
import com.zextras.lib.Error.ZxError;
import com.zextras.modules.chat.properties.ChatProperties;
import com.zextras.modules.chat.server.address.SpecificAddress;


public class MessageSizeExceededException extends ZxError
{
  public MessageSizeExceededException(SpecificAddress target, int length)
  {
    super(new ErrorCode()
    {
      public String getCodeString()
      {
        return "CHAT_MESSAGE_SIZE_EXCEEDED";
      }

      public String getMessage()
      {
        return "The message sent to {target} is {length} long and exceeds {max_size} max permitted size.";
      }
    });
    setDetail("target", target.toString());
    setDetail("length", length);
    setDetail("max_size", ChatProperties.MAX_MESSAGE_SIZE);
  }
}
