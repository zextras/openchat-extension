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

package com.zextras.modules.chat.server.parsing;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.zextras.lib.Optional;
import com.zextras.lib.activities.ActivityManager;
import com.zextras.modules.chat.properties.ChatProperties;
import com.zextras.modules.chat.server.UserCapabilitiesProvider;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.exceptions.ParserException;
import com.zextras.modules.chat.server.listener.PingQueueListener;
import com.zextras.modules.chat.server.listener.PingSoapQueueListener;
import com.zextras.modules.chat.server.listener.PingWebSocketQueueListener;
import com.zextras.modules.chat.server.operations.LastMessageInfoOperationFactory;
import com.zextras.modules.chat.server.operations.QueryArchiveFactory;
import com.zextras.modules.chat.server.session.SoapEventFilter;
import com.zextras.modules.chat.server.soap.SoapSessionFactory;
import com.zextras.modules.chat.server.soap.command.SoapCommand;
import com.zextras.modules.chat.server.soap.command.SoapCommandFriendAccept;
import com.zextras.modules.chat.server.soap.command.SoapCommandFriendAdd;
import com.zextras.modules.chat.server.soap.command.SoapCommandFriendBlock;
import com.zextras.modules.chat.server.soap.command.SoapCommandFriendRename;
import com.zextras.modules.chat.server.soap.command.SoapCommandMessageReceived;
import com.zextras.modules.chat.server.soap.command.SoapCommandPing;
import com.zextras.modules.chat.server.soap.command.SoapCommandQueryArchive;
import com.zextras.modules.chat.server.soap.command.SoapCommandRegister;
import com.zextras.modules.chat.server.soap.command.SoapCommandRemoveFriend;
import com.zextras.modules.chat.server.soap.command.SoapCommandRenameGroup;
import com.zextras.modules.chat.server.soap.command.SoapCommandSendMessage;
import com.zextras.modules.chat.server.soap.command.SoapCommandSendWriting;
import com.zextras.modules.chat.server.soap.command.SoapCommandSetAutoAway;
import com.zextras.modules.chat.server.soap.command.SoapCommandSetStatus;
import com.zextras.modules.chat.server.soap.command.SoapCommandUnblockUser;
import com.zextras.modules.chat.server.soap.command.SoapCommandUnregister;
import com.zextras.modules.chat.server.soap.encoders.SoapEncoderFactory;
import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;
import org.openzal.zal.Provisioning;
import org.openzal.zal.lib.Clock;
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
  public final static String ACTION_MESSAGE_RECEIVED      = "notify_msg_received";
  public final static String ACTION_SET_AUTO_AWAY         = "set_auto_away";
  public final static String ACTION_RENAME_GROUP          = "rename_group";
  public final static String ACTION_QUERY_ARCHIVE         = "query_archive";

  private final Map<String, String> mParameterMap;
  final Provisioning mProvisioning;
  final         SoapSessionFactory mSoapSessionFactory;
  final         Optional<ZimbraContext>      mZimbraContext;
  final         SoapResponse       mSoapResponse;
  private final Clock mClock;
  private final QueryArchiveFactory mQueryArchiveFactory;
  private final UserCapabilitiesProvider mUserCapabilitiesProvider;
  private final LastMessageInfoOperationFactory mLastMessageInfoOperationFactory;
  private final SoapEventFilter mSoapEventFilter;
  final         ChatProperties     mChatProperties;
  private final ActivityManager    mActivityManager;

  private final Optional<SpecificAddress> mSender;
  final         SoapEncoderFactory          mSoapEncoderFactory;
  private final Map<String, CommandCreator> mCommandCreatorMap;
  private final Optional<Channel> mChannel;

  @Inject
  public SoapParser(
    @Assisted("senderAddress") Optional<SpecificAddress> sender,
    @Assisted("zimbraContext") Optional<ZimbraContext> zimbraContext,
    @Assisted("channel") Optional<Channel> channel,
    @Assisted("parameterMap") Map<String, String> parameterMap,
    @Assisted("soapResponse") SoapResponse soapResponse,
    Provisioning provisioning,
    SoapEncoderFactory soapEncoderFactory,
    SoapSessionFactory soapSessionFactory,
    ChatProperties chatProperties,
    ActivityManager activityManager,
    Clock clock,
    QueryArchiveFactory queryArchiveFactory,
    UserCapabilitiesProvider userCapabilitiesProvider,
    LastMessageInfoOperationFactory lastMessageInfoOperationFactory,
    SoapEventFilter soapEventFilter
  )
  {
    mParameterMap = parameterMap;
    mProvisioning = provisioning;
    mChatProperties = chatProperties;
    mActivityManager = activityManager;
    mChannel = channel;
    mSender = sender;
    mSoapEncoderFactory = soapEncoderFactory;
    mSoapSessionFactory = soapSessionFactory;
    mZimbraContext = zimbraContext;
    mSoapResponse = soapResponse;
    mClock = clock;
    mQueryArchiveFactory = queryArchiveFactory;
    mUserCapabilitiesProvider = userCapabilitiesProvider;
    mLastMessageInfoOperationFactory = lastMessageInfoOperationFactory;
    mSoapEventFilter = soapEventFilter;
    mCommandCreatorMap = new HashMap<String, CommandCreator>(32);
    setupCommands();
  }

  void setupCommand(String command, CommandCreator commandCreator)
  {
    mCommandCreatorMap.put(command, commandCreator);
  }

  private void setupCommands()
  {
    if(mSender.hasValue())
    {
      final SpecificAddress mSenderAddress = mSender.getValue();
      setupCommand(ACTION_ADD_FRIEND, new CommandCreator()
      {
        @Override
        public SoapCommand create(Map<String, String> commandParameters)
        {
          return new SoapCommandFriendAdd(
            mSenderAddress,
            commandParameters,
            mProvisioning,
            mUserCapabilitiesProvider
          );
        }
      });
      setupCommand(ACTION_BLOCK_FRIEND, new CommandCreator()
      {
        @Override
        public SoapCommand create(Map<String, String> commandParameters)
        { return new SoapCommandFriendBlock(mSenderAddress, commandParameters); }
      });
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
            mZimbraContext.getValue(),
            mChatProperties,
            mLastMessageInfoOperationFactory,
            mSoapEventFilter
          );
        }
      });


      setupCommand(ACTION_SET_AUTO_AWAY, new CommandCreator()
      {
        @Override
        public SoapCommand create(Map<String, String> commandParameters)
        { return new SoapCommandSetAutoAway(mSenderAddress, commandParameters); }
      });
      setupCommand(ACTION_LOGOUT, new CommandCreator()
      {
        @Override
        public SoapCommand create(Map<String, String> commandParameters)
        { return new SoapCommandUnregister(mSenderAddress, commandParameters); }
      });
      setupCommand(ACTION_PING, new CommandCreator()
      {
        @Override
        public SoapCommand create(Map<String, String> commandParameters)
        {
          return new SoapCommandPing(
            mSoapResponse,
            mSoapEncoderFactory,
            mSenderAddress,
            commandParameters,
            mActivityManager,
            mZimbraContext,
            mChannel,
            mParameterMap
          );
        }
      });
      setupCommand(ACTION_REMOVE_FRIEND, new CommandCreator()
      {
        @Override
        public SoapCommand create(Map<String, String> commandParameters)
        { return new SoapCommandRemoveFriend(mSenderAddress, commandParameters); }
      });
      setupCommand(ACTION_SEND_MESSAGE, new CommandCreator()
      {
        @Override
        public SoapCommand create(Map<String, String> commandParameters)
        { return new SoapCommandSendMessage(mSoapResponse, mSenderAddress, commandParameters, mClock); }
      });
      setupCommand(ACTION_PING_WRITING, new CommandCreator()
      {
        @Override
        public SoapCommand create(Map<String, String> commandParameters)
        { return new SoapCommandSendWriting(mSenderAddress, commandParameters); }
      });
      setupCommand(ACTION_SET_STATUS, new CommandCreator()
      {
        @Override
        public SoapCommand create(Map<String, String> commandParameters)
        { return new SoapCommandSetStatus(mSenderAddress, commandParameters, mClock); }
      });
      setupCommand(ACTION_UNBLOCK_FRIEND, new CommandCreator()
      {
        @Override
        public SoapCommand create(Map<String, String> commandParameters)
        { return new SoapCommandUnblockUser(mSenderAddress, commandParameters); }
      });
      setupCommand(ACTION_MESSAGE_RECEIVED, new CommandCreator()
      {
        @Override
        public SoapCommand create(Map<String, String> commandParameters)
        { return new SoapCommandMessageReceived(mSenderAddress, commandParameters); }
      });
      setupCommand(ACTION_ACCEPT_FRIEND, new CommandCreator()
      {
        @Override
        public SoapCommand create(Map<String, String> commandParameters)
        { return new SoapCommandFriendAccept(mSenderAddress, commandParameters, mProvisioning); }
      });
      setupCommand(ACTION_RENAME_FRIEND, new CommandCreator()
      {
        @Override
        public SoapCommand create(Map<String, String> commandParameters)
        {
          return new SoapCommandFriendRename(
            mSenderAddress,
            commandParameters,
            mProvisioning,
            mUserCapabilitiesProvider
          );
        }
      });
      setupCommand(ACTION_RENAME_GROUP, new CommandCreator()
      {
        @Override
        public SoapCommand create(Map<String, String> commandParameters)
        { return new SoapCommandRenameGroup(mSenderAddress, commandParameters); }
      });
      setupCommand(ACTION_QUERY_ARCHIVE, new CommandCreator()
      {
        @Override
        public SoapCommand create(Map<String, String> commandParameters)
        {
          return new SoapCommandQueryArchive(
            mQueryArchiveFactory,
            mSoapResponse,
            mSenderAddress,
            commandParameters
          );
        }
      });
    }
  }

  @Override
  public SoapCommand parse()
    throws ParserException
  {
    final String evAction = mParameterMap.get("action");
    if( StringUtils.isBlank(evAction) ) {
      throw new ParserException("Missing attribute [action] from request");
    }

    if( !mCommandCreatorMap.containsKey(evAction) )
    {
      throw new ParserException("Invalid '" + evAction + "'.");
    }

    CommandCreator creatorCreator = mCommandCreatorMap.get(evAction);
    return creatorCreator.create(mParameterMap);
  }
}
