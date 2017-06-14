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

package com.zextras.modules.chat.server.soap.encoders;

import com.zextras.lib.json.JSONObject;
import com.zextras.modules.chat.server.ChatVersion;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.response.ChatSoapResponse;
import com.zextras.modules.chat.server.soap.SoapEncoder;
import com.zextras.modules.chat.server.events.EventSoapSessionRegistered;
import org.openzal.zal.lib.Version;

public class EventSoapSessionRegistredEncoder implements SoapEncoder
{
  private final EventSoapSessionRegistered mEventRegister;
  private final ChatVersion mChatVersion;

  public EventSoapSessionRegistredEncoder(
    EventSoapSessionRegistered eventRegister,
    ChatVersion chatVersion
  )
  {
    mEventRegister = eventRegister;
    mChatVersion = chatVersion;
  }

  public void encode(ChatSoapResponse response, SpecificAddress target)
  {
    final JSONObject registerResponse = new JSONObject();

    registerResponse.put("session_id", mEventRegister.getSessionId().toString());
    registerResponse.put("server_version", mChatVersion.getServerChatVersion().toString());
    registerResponse.put("required_zimlet_version", mChatVersion.getRequiredZimletVersion().toString());
    registerResponse.put("history_enabled",  mEventRegister.isHistoryEnabled());
    /*registerResponse.put("remove_brand", mEventRegister.removeBrand());
    registerResponse.put("videochat_enabled", mEventRegister.isVideoChatEnabled());*/
    registerResponse.put("silent_error_reporting_enabled", mEventRegister.isSilentErrorReportingEnabled());
    registerResponse.put("room_service_address", mEventRegister.getRoomServiceAddress().toString());

    if( mEventRegister.getClientVersion().equals(new Version(0)) ){
      response.enableBackcompatibilityHack();
    }

    response.addResponse(registerResponse);
  }
}
