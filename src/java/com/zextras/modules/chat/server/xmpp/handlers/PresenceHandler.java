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

package com.zextras.modules.chat.server.xmpp.handlers;

import com.zextras.modules.chat.server.status.Status;
import com.zextras.modules.chat.server.status.VolatileStatus;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.operations.*;
import com.zextras.modules.chat.server.xmpp.StanzaHandler;
import com.zextras.modules.chat.server.xmpp.XmppSession;
import com.zextras.modules.chat.server.xmpp.decoders.PresenceDecoder;
import com.zextras.modules.chat.server.xmpp.netty.StanzaProcessor;
import com.zextras.modules.chat.server.xmpp.parsers.PresenceParser;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.openzal.zal.Provisioning;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.util.*;

public class PresenceHandler implements StanzaHandler
{
  private final XmppSession                           mSession;
  private final Provisioning                          mProvisioning;
  private final StanzaProcessor.XmppConnectionHandler mConnectionHandler;

  private PresenceParser mParser = null;

  public PresenceHandler(
    StanzaProcessor.XmppConnectionHandler connectionHandler,
    Provisioning provisioning
  )
  {
    mSession = connectionHandler.getSession();
    mConnectionHandler = connectionHandler;
    mProvisioning = provisioning;
  }

  public List<ChatOperation> handleSubscribed()
  {
    if( mParser.getType().equals("subscribed") )
    {
      ChatOperation operationFriendAccept = new AcceptFriend(
        mSession.getExposedAddress(),
        new SpecificAddress(mParser.getTo()),
        mProvisioning
      );

      return Collections.<ChatOperation>singletonList(operationFriendAccept);
    }

    return Collections.emptyList();
  }

  public List<ChatOperation> handleFirstPresence()
  {
    if (mSession.isFirstPresence())
    {
      mSession.setFirstPresence(false);
      ChatOperation notifyFriendsStatus = new NotifyFriendsStatus(
        mSession.getExposedAddress()
      );
      return Collections.<ChatOperation>singletonList(notifyFriendsStatus);
    }

    return Collections.emptyList();
  }

  public List<ChatOperation> handleSubscribe()
  {
    if( mParser.getType().equals("subscribe") )
    {
      ChatOperation operationFriendAccept = new AddFriend(
        mSession.getExposedAddress(),
        new SpecificAddress(mParser.getTo()),
        "",
        "",
        mProvisioning
      );

      return Collections.<ChatOperation>singletonList(operationFriendAccept);
    }

    return Collections.emptyList();
  }

  public List<ChatOperation> handleUnsubscribe()
  {
    if( mParser.getType().equals("unsubscribe") )
    {
      ChatOperation operation = new RemoveFriend(
        mSession.getExposedAddress(),
        new SpecificAddress(mParser.getTo())
      );
      return Collections.<ChatOperation>singletonList(operation);
    }

    return Collections.emptyList();
  }

  public List<ChatOperation> handleInvisible()
  {
    if( mParser.getType().equals("invisible") )
    {
      Status.StatusType type = Status.StatusType.INVISIBLE;
      Status newStatus = new VolatileStatus(type, mParser.getStatusText());

      ChatOperation setStatus = new SetStatus(
        mSession.getId(),
        newStatus
      );
      return Collections.<ChatOperation>singletonList(setStatus);
    }

    return Collections.emptyList();
  }

  public List<ChatOperation> handleUnavailable()
  {
    if( mParser.getType().equals("unavailable") )
    {
      mSession.getEventQueue().removeListener();
      mConnectionHandler.close();
      return Collections.emptyList();
    }

    return Collections.emptyList();
  }

  public List<ChatOperation> handleStatusType()
  {
    if( mParser.getType().isEmpty() )
    {
      Status.StatusType type = PresenceDecoder.sShowToStatusMap.get(mParser.getShow());
      if( type == null ) {
        type = Status.StatusType.AVAILABLE;
      }

      Status newStatus = new VolatileStatus(type, mParser.getStatusText());

      SetStatus setStatus = new SetStatus(
        mSession.getId(),
        newStatus
      );
      return Collections.<ChatOperation>singletonList(setStatus);
    }

    return Collections.emptyList();
  }

  @Override
  public List<ChatOperation> handle()
  {
    List<ChatOperation> operations = new ArrayList<ChatOperation>();

    operations.addAll(handleFirstPresence());
    operations.addAll(handleSubscribed());
    operations.addAll(handleSubscribe());
    operations.addAll(handleUnsubscribe());
    operations.addAll(handleInvisible());
    operations.addAll(handleUnavailable());
    operations.addAll(handleStatusType());

    return operations;
  }

  @Override
  public void parse(ByteArrayInputStream xmlInputStream, SchemaProvider schemaProvider) throws XMLStreamException
  {
    mParser = new PresenceParser(xmlInputStream, schemaProvider);
    mParser.parse();
  }

  public void setParser(PresenceParser parser)
  {
    mParser = parser;
  }
}
