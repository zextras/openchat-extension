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

package com.zextras.modules.chat.server.soap.encoders;

import com.zextras.lib.Error.ZxError;
import com.zextras.lib.json.JSONObject;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.client_contstants.ClientEventType;
import com.zextras.modules.chat.server.response.ChatSoapResponse;
import com.zextras.modules.chat.server.soap.SoapEncoder;


public class GenericSoapErrorEncoder implements SoapEncoder
{
  private final ZxError mEventError;

  public GenericSoapErrorEncoder(ZxError eventError)
  {
    mEventError = eventError;
  }

  @Override
  public void encode(
    ChatSoapResponse response, SpecificAddress target
  )
  {
    final JSONObject message = mEventError.toJSON();
    message.put("type", ClientEventType.ERROR);
    response.addResponse(message);
  }
}
