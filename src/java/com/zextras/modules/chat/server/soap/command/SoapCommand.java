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

package com.zextras.modules.chat.server.soap.command;

import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.session.SessionUUID;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.address.SpecificAddressFromSession;
import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.exceptions.InvalidParameterException;
import com.zextras.modules.chat.server.exceptions.MissingParameterException;

import java.util.List;
import java.util.Map;

public abstract class SoapCommand
{
  /**
   * Equivalent to TO
   */
  protected final static String TARGET_ADDRESS   = "target_address";
  /**
   * Same as TARGET_ADDRESS
   */
  protected final static String TO                               = "to";
  protected final static String TARGET_USERNAME                  = "target_username";
  protected final static String TARGET_GROUP                     = "target_group";
  protected final static String TARGET_NEW_GROUP                 = "new_group";
  protected final static String MESSAGE                          = "message";
  protected final static String MESSAGE_ID                       = "message_id";
  protected final static String SESSION_ID                       = "session_id";
  public final static String STATUS_ID                           = "status_id";
  protected final static String VALUE                            = "value";
  protected final static String SESSION_SUCCESSFULLY_SENT_EVENTS = "received_events";
  public static final String TOPIC                               = "topic";

  protected final SpecificAddress     mSenderAddress;
  protected final Map<String, String> mParameterMap;

  public SoapCommand(SpecificAddress senderAddress, Map<String, String> parameters)
  {
    mParameterMap = parameters;
    mSenderAddress = getSessionSpecificAddress(senderAddress);
  }

  private SpecificAddress getSessionSpecificAddress(SpecificAddress senderAddress)
  {
    String sessionUUID = mParameterMap.get(SESSION_ID);
    if (sessionUUID != null)
    {
      return new SpecificAddressFromSession(senderAddress.toString(), sessionUUID, SessionUUID.fromString(sessionUUID));
    }
    return senderAddress;
  }

  public String getTargetUsername()
  {
    if(mParameterMap.get(TARGET_USERNAME) != null)
    {
      return mParameterMap.get(TARGET_USERNAME);
    }
    return "";
  }

  public String getTargetGroup()
  {
    String group = mParameterMap.get(TARGET_GROUP);
    return group == null ? "" : group;
  }

  public abstract List<ChatOperation> createOperationList()
    throws MissingParameterException, InvalidParameterException, ChatException;

  public SpecificAddress getSenderAddress()
  {
    return mSenderAddress;
  }


  protected SpecificAddress getTargetAddress() throws MissingParameterException
  {
    final String targetAddressString;

    if( mParameterMap.containsKey(TARGET_ADDRESS) ) {
      targetAddressString = mParameterMap.get(TARGET_ADDRESS);
    }
    else
    {
      targetAddressString = mParameterMap.get(TO);
    }

    if( targetAddressString == null ) {
      throw new MissingParameterException("Missing parameters to create " + getClass().getName());
    }

    return new SpecificAddress(targetAddressString);
  }
}
