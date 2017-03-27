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

package com.zextras.modules.chat.server.parsing;

import com.zextras.modules.chat.properties.ChatProperties;
import com.zextras.lib.activities.ActivityManager;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.soap.SoapSessionFactory;
import com.zextras.modules.chat.server.soap.encoders.SoapEncoderFactory;
import com.zextras.modules.chat.server.soap.encoders.SoapEncoderFactoryImpl;
import com.zextras.modules.chat.server.soap.command.*;
import com.zextras.modules.chat.server.exceptions.ParserException;
import org.openzal.zal.Provisioning;
import org.openzal.zal.soap.SoapResponse;
import org.openzal.zal.soap.ZimbraContext;

import java.util.HashMap;
import java.util.Map;

public class SoapParser implements Parser
{
  public final static String ACTION_ADD_FRIEND            = "add_friend";
  public final static String ACTION_BLOCK_FRIEND          = "block_friend";
  public final static String ACTION_ACCEPT_FRIEND         = "accept_friend";
  public final static String ACTION_REMOVE_FRIEND         = "remove_friend";
  public final static String ACTION_RENAME_FRIEND         = "rename_friend";
  public final static String ACTION_REQUEST_RELATIONSHIPS = "request_relationships";
  public final static String ACTION_REQUEST_STATUSES      = "request_statuses";
  public final static String ACTION_LOGIN                 = "register_session";
  public final static String ACTION_LOGOUT                = "unregister_session";
  public final static String ACTION_PING                  = "ev_ping";
  public final static String ACTION_SEND_MESSAGE          = "send_message";
  public final static String ACTION_PING_WRITING          = "ping_writing";
  public final static String ACTION_SET_STATUS            = "set_user_status";
  public final static String ACTION_UNBLOCK_FRIEND        = "unblock_friend";
  public final static String ACTION_WINDOW_CLOSED         = "chat_window_closed";
  public final static String ACTION_MESSAGE_RECEIVED      = "notify_msg_received";
  public final static String ACTION_SET_AUTO_AWAY         = "set_auto_away";
  public final static String ACTION_RENAME_GROUP          = "rename_group";

  final         Provisioning       mProvisioning;
  final         SoapSessionFactory mSoapSessionFactory;
  final         ZimbraContext      mZimbraContext;
  final         SoapResponse       mSoapResponse;
  final         ChatProperties     mChatProperties;
  private final ActivityManager    mActivityManager;

  final         SpecificAddress             mSenderAddress;
  final         SoapEncoderFactory          mSoapEncoderFactory;
  private final Map<String, CommandCreator> mCommandCreatorMap;

  public SoapParser(
    Provisioning provisioning,
    SoapEncoderFactory soapEncoderFactory,
    SoapSessionFactory soapSessionFactory,
    ChatProperties chatProperties,
    ActivityManager activityManager,
    SpecificAddress senderAddress,
    ZimbraContext zimbraContext,
    SoapResponse soapResponse
  )
  {
    mProvisioning = provisioning;
    mChatProperties = chatProperties;
    mActivityManager = activityManager;
    mSenderAddress = senderAddress;
    mSoapEncoderFactory = soapEncoderFactory;
    mSoapSessionFactory = soapSessionFactory;
    mZimbraContext = zimbraContext;
    mSoapResponse = soapResponse;
    mCommandCreatorMap = new HashMap<String, CommandCreator>(32);
    setupCommands();
  }

  void setupCommand(String command, CommandCreator commandCreator)
  {
    mCommandCreatorMap.put(command, commandCreator);
  }

  private void setupCommands()
  {
    setupCommand(ACTION_ADD_FRIEND, new CommandCreator()
    {
      @Override
      public SoapCommand create(Map<String, String> commandParameters)
      { return new SoapCommandFriendAdd(mSenderAddress, commandParameters, mProvisioning); }
    } );
    setupCommand(ACTION_BLOCK_FRIEND, new CommandCreator()
    {
      @Override
      public SoapCommand create(Map<String, String> commandParameters)
      { return new SoapCommandFriendBlock(mSenderAddress, commandParameters); }
    } );
    setupCommand(ACTION_LOGIN, new CommandCreator()
    {
      @Override
      public SoapCommand create(Map<String, String> commandParameters)
      {
        return new SoapCommandRegister(
          mSoapResponse,
          mSoapEncoderFactory,
          mSenderAddress,
          commandParameters,
          mSoapSessionFactory,
          mProvisioning,
          mZimbraContext,
          mChatProperties
        );
      }
    });

    setupCommand(ACTION_LOGOUT, new CommandCreator()
    {
      @Override
      public SoapCommand create(Map<String, String> commandParameters)
      { return new SoapCommandUnregister(mSenderAddress,commandParameters); }
    });
    setupCommand(ACTION_PING, new CommandCreator()
    {
      @Override
      public SoapCommand create(Map<String, String> commandParameters)
      { return new SoapCommandPing(mSoapResponse, mSoapEncoderFactory, mSenderAddress, commandParameters, mZimbraContext, mActivityManager); }
    });
    setupCommand(ACTION_REMOVE_FRIEND, new CommandCreator()
    {
      @Override
      public SoapCommand create(Map<String, String> commandParameters)
      { return new SoapCommandRemoveFriend(mSenderAddress,commandParameters); }
    });
    setupCommand(ACTION_SEND_MESSAGE, new CommandCreator()
    {
      @Override
      public SoapCommand create(Map<String, String> commandParameters)
      { return new SoapCommandSendMessage(mSoapResponse,mSenderAddress,commandParameters); }
    });
    setupCommand(ACTION_PING_WRITING, new CommandCreator()
    {
      @Override
      public SoapCommand create(Map<String, String> commandParameters)
      { return new SoapCommandSendWriting(mSenderAddress,commandParameters); }
    });
    setupCommand(ACTION_SET_STATUS, new CommandCreator()
    {
      @Override
      public SoapCommand create(Map<String, String> commandParameters)
      { return new SoapCommandSetStatus(mSenderAddress,commandParameters); }
    });
    setupCommand(ACTION_UNBLOCK_FRIEND, new CommandCreator()
    {
      @Override
      public SoapCommand create(Map<String, String> commandParameters)
      { return new SoapCommandUnblockUser(mSenderAddress,commandParameters); }
    });
    setupCommand(ACTION_WINDOW_CLOSED, new CommandCreator()
    {
      @Override
      public SoapCommand create(Map<String, String> commandParameters)
      { return new SoapCommandWindowClosed(mSenderAddress,commandParameters); }
    });
    setupCommand(ACTION_MESSAGE_RECEIVED, new CommandCreator()
    {
      @Override
      public SoapCommand create(Map<String, String> commandParameters)
      { return new SoapCommandMessageReceived(mSenderAddress,commandParameters); }
    });
    setupCommand(ACTION_ACCEPT_FRIEND, new CommandCreator()
    {
      @Override
      public SoapCommand create(Map<String, String> commandParameters)
      { return new SoapCommandFriendAccept( mSenderAddress,commandParameters, mProvisioning); }
    });
    setupCommand(ACTION_SET_AUTO_AWAY, new CommandCreator()
    {
      @Override
      public SoapCommand create(Map<String, String> commandParameters)
      { return new SoapCommandSetAutoAway(mSenderAddress,commandParameters); }
    });
    setupCommand(ACTION_RENAME_FRIEND, new CommandCreator()
    {
      @Override
      public SoapCommand create(Map<String, String> commandParameters)
      { return new SoapCommandFriendRename( mSenderAddress,commandParameters, mProvisioning); }
    });
    setupCommand(ACTION_RENAME_GROUP, new CommandCreator()
    {
      @Override
      public SoapCommand create(Map<String, String> commandParameters)
      { return new SoapCommandRenameGroup( mSenderAddress,commandParameters); }
    });
  }

  @Override
  public SoapCommand parse()
    throws ParserException
  {
    final String evAction;

    evAction = mZimbraContext.getParameter("action", "");
    if( evAction.isEmpty() ) {
      throw new ParserException("Missing attribute [action] from request");
    }

    if( !mCommandCreatorMap.containsKey(evAction) )
    {
      throw new ParserException("Invalid '" + evAction + "'.");
    }

    CommandCreator creatorCreator = mCommandCreatorMap.get(evAction);
    final Map<String, String> commandParameters = mZimbraContext.getParameterMap();

    return creatorCreator.create(commandParameters);
  }
}
