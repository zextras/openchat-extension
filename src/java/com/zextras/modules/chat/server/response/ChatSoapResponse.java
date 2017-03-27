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

package com.zextras.modules.chat.server.response;

import com.zextras.lib.json.JSONArray;
import com.zextras.lib.json.JSONObject;
import org.openzal.zal.soap.SoapResponse;

/**
 * This class contains a ping response.
 * Note that a ping response can contain multiple
 * responses to different commands that are
 * being answered to by the same ping.
 *
 */
public class ChatSoapResponse
{
  private final JSONArray mResponses;
  private       boolean   mBackcompatibilityHack;

  public ChatSoapResponse()
  {
    mResponses = new JSONArray();
    mBackcompatibilityHack = false;
  }

  public void addResponse(JSONObject response)
  {
    mResponses.put(response);
  }

  public void encodeInSoapResponse(SoapResponse soapResponse)
  {
    if( mBackcompatibilityHack )
    {
      JSONObject obj = mResponses.getJSONObject(0);
      for( String key : obj.keySet() ) {
        soapResponse.setValue(key, obj.get(key).toString());
      }
    }
    else
    {
      soapResponse.setValue("responses", mResponses.toString());
    }
  }

  public void enableBackcompatibilityHack()
  {
    mBackcompatibilityHack = true;
  }

  public String toTestJson()
  {
    return mResponses.toString();
  }

  public String toPrettyJson()
  {
    return mResponses.toPrettyString();
  }

  public JSONArray getJson()
  {
    return mResponses;
  }
}
