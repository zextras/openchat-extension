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

package com.zextras.modules.chat.server.xmpp.encoders;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zextras.modules.chat.server.encoding.Encoder;
import com.zextras.modules.chat.server.events.*;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;

@Singleton
public class XmppEncoderFactoryImpl implements XmppEncoderFactory
{
  private final SchemaProvider mSchemaProvider;

  @Inject
  public XmppEncoderFactoryImpl(SchemaProvider schemaProvider) {
    mSchemaProvider = schemaProvider;
  }

  //soap events
  @Override
  public Encoder interpret(EventSoapSessionRegistered eventSoapSessionRegistered)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Encoder interpret(Event event)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Encoder interpret(EventBootCompleted eventBootCompleted) throws ChatException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Encoder interpret(EventNewClientVersion eventNewClientVersion)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Encoder interpret(EventMessageSizeExceeded eventMessageSizeExceeded)
  {
    return new EventMessageSizeExceededEncoder(eventMessageSizeExceeded, mSchemaProvider);
  }

  @Override
  public Encoder interpret(FriendNotFoundEvent friendNotFoundEvent)
  {
    return new EventFriendNotFoundEncoder(friendNotFoundEvent, mSchemaProvider);
  }

  @Override
  public Encoder interpret(FeatureNotImplementedEvent featureNotImplementedEvent)
  {
    return new FeatureNotImplementedEncoder(featureNotImplementedEvent, mSchemaProvider);
  }

  @Override
  public Encoder interpret(EventStreamStarted eventXmppSessionRegistered)
  {
    return new EventStreamStartedEncoder(eventXmppSessionRegistered,mSchemaProvider);
  }

  @Override
  public Encoder interpret(EventIQAuthResult eventIqAuthResult)
  {
    return new EventIQAuthResultEncoder(eventIqAuthResult,mSchemaProvider);
  }

  @Override
  public Encoder interpret(EventXmppSASLAuthentication eventXmppSASLAuthentication)
  {
    return new XmppSASLAuthResultEncoder(eventXmppSASLAuthentication,mSchemaProvider);
  }

  @Override
  public Encoder interpret(EventXmppSessionFeatures eventXmppSessionFeatures)
  {
    return new XmppEventSessionFeaturesEncoder(eventXmppSessionFeatures,mSchemaProvider);
  }

  @Override
  public Encoder interpret(EventXmppRedirect eventXmppRedirect)
  {
    return new XmppRedirectResultEncoder(eventXmppRedirect, mSchemaProvider);
  }

  @Override
  public Encoder interpret(EventXmppSessionEstablished eventXmppSessionEstablished)
  {
    return new XmppSessionEstablishedEncoder(eventXmppSessionEstablished, mSchemaProvider);
  }

  @Override
  public Encoder interpret(EventXmppPing eventXmppPing)
  {
    return new PingEncoder(eventXmppPing, mSchemaProvider);
  }

  @Override
  public Encoder interpret(EventDiscovery event)
  {
    return new DiscoveryEncoder(event, mSchemaProvider);
  }

  @Override
  public Encoder interpret(EventFloodControl event)
  {
    return new EventFloodControlEncoder(event, mSchemaProvider);
  }

  @Override
  public Encoder interpret(EventIQQuery event) throws ChatException
  {
    return new EventIQQueryEncoder(event,mSchemaProvider);
  }

  @Override
  public Encoder interpret(EventMessageHistory event) throws ChatException
  {
    return new EventMessageHistoryEncoder(event,mSchemaProvider,this);
  }

  @Override
  public Encoder interpret(EventMessageHistoryLast event) throws ChatException
  {
    return new EventMessageHistoryLastEncoder(event,mSchemaProvider);
  }

  @Override
  public Encoder interpret(EventSharedFile event) throws ChatException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Encoder interpret(EventBindResult eventBindResult)
  {
    return new EventBindResultEncoder(eventBindResult,mSchemaProvider);
  }

  @Override
  public Encoder interpret(EventMessageAck eventMessageAck)
  {
    return new EventMessageAckEncoder(eventMessageAck, mSchemaProvider);
  }

  //common events
  @Override
  public Encoder interpret(EventIsWriting eventIsWriting)
  {
    return new EventIsWritingEncoder(eventIsWriting,mSchemaProvider);
  }

  @Override
  public Encoder interpret(EventFriendRenamed eventFriendRenamed)
  {
    return new XmppEventFriendRenamedEncoder(eventFriendRenamed, mSchemaProvider);
  }

  @Override
  public Encoder interpret(EventStatuses eventStatuses)
  {
    return null;
  }

  @Override
  public Encoder interpret(EventGetRelationships eventGetRelationships)
  {
    return new EventRelationshipsEncoder(eventGetRelationships,mSchemaProvider);
  }

  @Override
  public Encoder interpret(EventStatusChanged eventStatusChanged)
  {
    return new EventStatusChangedEncoder(eventStatusChanged,mSchemaProvider);
  }

  @Override
  public Encoder interpret(EventFriendAdded eventFriendAdded)
  {
    return new EventFriendAddedEncoder(eventFriendAdded,mSchemaProvider);
  }

  @Override
  public Encoder interpret(EventFriendBackAdded eventBackFriendAdded) {
    return new EventFriendBackAddedEncoder(eventBackFriendAdded, mSchemaProvider);
  }

  @Override
  public Encoder interpret(EventMessage eventMessage)
  {
    return new EventMessageEncoder(eventMessage,mSchemaProvider);
  }

  @Override
  public Encoder interpret(EventStatusProbe eventStatusProbe)
  {
    return new EventStatusProbeEncoder(eventStatusProbe,mSchemaProvider);
  }

  @Override
  public Encoder interpret(EventGetPrivacy eventGetPrivacy)
  {
    return new EventGetPrivacyEncoder(eventGetPrivacy,mSchemaProvider);
  }

  public Encoder interpret(EventMessageBack eventMessage)
  {
    return new EventMessageBackEncoder(eventMessage, mSchemaProvider);
  }

  @Override
  public Encoder interpret(EventFriendBackRemove eventFriendBackRemove) {
    return new EventFriendBackRemoveEncoder(eventFriendBackRemove, mSchemaProvider);
  }

  @Override
  public Encoder interpret(EventFriendAccepted eventFriendAccepted)
  {
    return new EventFriendAcceptedEncoder(eventFriendAccepted,mSchemaProvider);
  }

  @Override
  public Encoder interpret(EventFriendBackAccepted eventFriendBackAccepted) {
    return new EventFriendBackAcceptedEncoder(eventFriendBackAccepted, mSchemaProvider);
  }

  @Override
  public Encoder interpret(EventFriendAddedForClient eventFriendAddedForClient)
  {
    return new EventFriendAddedEncoder(eventFriendAddedForClient,mSchemaProvider);
  }
}
