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


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zextras.lib.json.JSONObject;
import com.zextras.modules.chat.server.ChatVersion;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.client_contstants.ClientEventType;
import com.zextras.modules.chat.server.encoding.Encoder;
import com.zextras.modules.chat.server.encoding.EncoderFactory;
import com.zextras.modules.chat.server.events.*;
import com.zextras.modules.chat.server.exceptions.MessageSizeExceededException;
import com.zextras.modules.chat.server.exceptions.NoSuchAccountChatException;
import com.zextras.modules.chat.server.response.ChatSoapResponse;
import com.zextras.modules.chat.server.soap.SoapEncoder;

@Singleton
public class SoapEncoderFactoryImpl implements SoapEncoderFactory
{
  private final ChatVersion mChatVersion;

  @Inject
  public SoapEncoderFactoryImpl(ChatVersion chatVersion)
  {
    mChatVersion = chatVersion;
  }

  //common events
  @Override
  public Encoder interpret(EventIsWriting eventIsWriting)
  {
    return new EventIsWritingEncoder(eventIsWriting);
  }

  @Override
  public Encoder interpret(EventFriendRenamed eventFriendRenamed)
  {
    return new EventFriendRenamedEncoder(eventFriendRenamed);
  }

  @Override
  public Encoder interpret(EventStatuses eventStatuses)
  {
    return new EventStatuesEncoder(eventStatuses);
  }

  @Override
  public Encoder interpret(EventGetRelationships eventGetRelationships)
  {
    return new EventRelationshipEncoder(eventGetRelationships);
  }

  @Override
  public Encoder interpret(EventStatusChanged eventStatusChanged)
  {
    return new EventStatusChangedEncoder(eventStatusChanged);
  }

  @Override
  public Encoder interpret(EventFriendAdded eventFriendAdded)
  {
    return new EventFriendAddedEncoder(eventFriendAdded);
  }

  @Override
  public Encoder interpret(EventFriendBackAdded eventBackFriendAdded) {
    return new EventFriendBackAddedEncoder(eventBackFriendAdded);
  }

  @Override
  public Encoder interpret(EventMessage eventMessage)
  {
    return new EventMessageEncoder(eventMessage);
  }

  @Override
  public Encoder interpret(EventFriendAccepted eventFriendAccepted)
  {
    return new EventFriendAcceptedEncoder(eventFriendAccepted);
  }

  @Override
  public Encoder interpret(EventFriendBackAccepted eventFriendBackAccepted) {
    return new EventFriendBackAcceptedEncoder(eventFriendBackAccepted);
  }

  @Override
  public Encoder interpret(EventFriendAddedForClient eventFriendAddedForClient)
  {
    return new EventFriendAddedEncoder(eventFriendAddedForClient);
  }

  @Override
  public Encoder interpret(EventMessageAck eventMessageAck)
  {
    return new EventMessageAckEncoder(eventMessageAck);
  }

  @Override
  public Encoder interpret(EventStatusProbe eventStatusProbe)
  {
    fail(eventStatusProbe);
    return null;
  }

  @Override
  public Encoder interpret(EventGetPrivacy eventGetPrivacy)
  {
    fail(eventGetPrivacy);
    return null;
  }

  public Encoder interpret(EventMessageBack eventMessage)
  {
    return new EventMessageBackEncoder(eventMessage);
  }

  @Override
  public Encoder interpret(EventFriendBackRemove eventFriendBackRemove)
  {
    return new EventFriendBackRemoveEncoder(eventFriendBackRemove);
  }

  @Override
  public Encoder interpret(EventNewClientVersion eventNewClientVersion)
  {
    return new EventNewClientVersionEncoder(eventNewClientVersion);
  }

  @Override
  public Encoder interpret(EventMessageSizeExceeded eventMessageSizeExceeded)
  {
    return new GenericSoapErrorEncoder(
      new MessageSizeExceededException(
        eventMessageSizeExceeded.getMessageTo(),
        eventMessageSizeExceeded.getLength()
      )
    );
  }

  @Override
  public Encoder interpret(FriendNotFoundEvent friendNotFoundEvent)
  {
    return new GenericSoapErrorEncoder(new NoSuchAccountChatException(friendNotFoundEvent.getFrientToAdd().toString()));
  }

  @Override
  public Encoder interpret(FeatureNotImplementedEvent event)
  {
    fail(event);
    return null;
  }

  @Override
  public Encoder interpret(EventStreamStarted event)
  {
    fail(event);
    return null;
  }

  @Override
  public Encoder interpret(EventIQAuthResult event)
  {
    fail(event);
    return null;
  }

  @Override
  public Encoder interpret(EventXmppSASLAuthentication event)
  {
    fail(event);
    return null;
  }

  @Override
  public Encoder interpret(EventXmppSessionFeatures event)
  {
    fail(event);
    return null;
  }

  @Override
  public Encoder interpret(EventXmppRedirect event)
  {
    fail(event);
    return null;
  }

  @Override
  public Encoder interpret(EventXmppSessionEstablished event)
  {
    fail(event);
    return null;
  }

  @Override
  public Encoder interpret(EventXmppPing event)
  {
    fail(event);
    return null;
  }

  @Override
  public Encoder interpret(EventXmppDiscovery event)
  {
    fail(event);
    return null;
  }

  @Override
  public Encoder interpret(final EventFloodControl event)
  {
    fail(event);
    return null;
  }

  void fail(Event event)
  {
    throw new RuntimeException("invalid encoding of event "+event.getClass().getName());
  }

  //soap events
  @Override
  public Encoder interpret(EventSoapSessionRegistred eventSoapSessionRegistred)
  {
    return new EventSoapSessionRegistredEncoder(eventSoapSessionRegistred, mChatVersion);
  }

  @Override
  public Encoder interpret(EventBindResult eventBindResult)
  {
    fail(eventBindResult);
    return null;
  }
}
