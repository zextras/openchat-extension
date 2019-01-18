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

package com.zextras.modules.chat;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.zextras.lib.ZimbraSSLContextProvider;
import com.zextras.modules.chat.properties.ChatProperties;
import com.zextras.modules.chat.properties.LdapChatProperties;
import com.zextras.modules.chat.server.UserCapabilitiesProvider;
import com.zextras.modules.chat.server.UserCapabilitiesProviderImpl;
import com.zextras.modules.chat.server.ChatVersion;
import com.zextras.modules.chat.server.OpenchatVersion;
import com.zextras.modules.chat.server.LocalXmppConnectionProvider;
import com.zextras.modules.chat.server.LocalXmppConnectionProviderImpl;
import com.zextras.modules.chat.server.address.AddressResolver;
import com.zextras.modules.chat.server.address.AddressResolverStub;
import com.zextras.modules.chat.server.db.ChatDbHandler;
import com.zextras.modules.chat.server.db.ChatMariaDbHandler;
import com.zextras.modules.chat.server.db.mappers.OpenStatementsFactory;
import com.zextras.modules.chat.server.db.mappers.StatementsFactory;
import com.zextras.modules.chat.server.db.modifiers.OpenUserModifier;
import com.zextras.modules.chat.server.db.modifiers.UserModifier;
import com.zextras.modules.chat.server.db.providers.OpenUserProvider;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.dispatch.RoomServerHostSetProvider;
import com.zextras.modules.chat.server.dispatch.StubRoomServerHostSetProvider;
import com.zextras.modules.chat.server.events.EventInterceptorFactory;
import com.zextras.modules.chat.server.events.EventQueueFactory;
import com.zextras.modules.chat.server.events.EventQueueFilterEvent;
import com.zextras.modules.chat.server.events.EventQueueFilterEventImpl;
import com.zextras.modules.chat.server.history.HistoryMessageBuilder;
import com.zextras.modules.chat.server.history.HistoryMessageBuilderImpl;
import com.zextras.modules.chat.server.interceptors.UserEventInterceptorFactory;
import com.zextras.modules.chat.server.interceptors.QueryArchiveInterceptorFactory;
import com.zextras.modules.chat.server.interceptors.QueryArchiveInterceptorFactoryImpl;
import com.zextras.modules.chat.server.operations.LastMessageInfoOperationFactory;
import com.zextras.modules.chat.server.operations.QueryArchiveFactory;
import com.zextras.modules.chat.server.parsing.Parser;
import com.zextras.modules.chat.server.parsing.ParserFactory;
import com.zextras.modules.chat.server.parsing.SoapParser;
import com.zextras.modules.chat.server.relationship.RelationshipModifier;
import com.zextras.modules.chat.server.relationship.RelationshipModifierProxy;
import com.zextras.modules.chat.server.relationship.RelationshipProvider;
import com.zextras.modules.chat.server.relationship.RelationshipProviderCombiner;
import com.zextras.modules.chat.server.session.CommonSessionEventFilter;
import com.zextras.modules.chat.server.session.CommonSessionEventFilterImpl;
import com.zextras.modules.chat.server.soap.InitialSoapRequestHandlerFactory;
import com.zextras.modules.chat.server.soap.SoapFilter;
import com.zextras.modules.chat.server.soap.SoapFilterImpl;
import com.zextras.modules.chat.server.soap.SoapHandlerCreatorFactory;
import com.zextras.modules.chat.server.soap.SoapSessionFactory;
import com.zextras.modules.chat.server.soap.encoders.SoapEncoderFactory;
import com.zextras.modules.chat.server.soap.encoders.SoapEncoderFactoryImpl;
import com.zextras.modules.chat.server.xmpp.StanzaRecognizer;
import com.zextras.modules.chat.server.xmpp.StanzaRecognizerImpl;
import com.zextras.modules.chat.server.xmpp.StanzaWriterFactory;
import com.zextras.modules.chat.server.xmpp.StanzaWriterFactoryImpl;
import com.zextras.modules.chat.server.xmpp.XmppEventFactory;
import com.zextras.modules.chat.server.xmpp.XmppEventFactoryImpl;
import com.zextras.modules.chat.server.xmpp.XmppFilter;
import com.zextras.modules.chat.server.xmpp.XmppFilterImpl;
import com.zextras.modules.chat.server.xmpp.XmppFilterOut;
import com.zextras.modules.chat.server.xmpp.XmppFilterOutImpl;
import com.zextras.modules.chat.server.xmpp.XmppHandlerFactory;
import com.zextras.modules.chat.server.xmpp.XmppHandlerFactoryImpl;
import com.zextras.modules.chat.server.xmpp.encoders.XmppEncoderFactory;
import com.zextras.modules.chat.server.xmpp.encoders.XmppEncoderFactoryImpl;
import com.zextras.modules.core.ProvisioningCache;
import org.openzal.zal.MailboxManager;
import org.openzal.zal.Provisioning;
import org.openzal.zal.ZimbraConnectionProvider;
import org.openzal.zal.extension.Zimbra;
import org.openzal.zal.lib.ZimbraDatabase;
import org.openzal.zal.lib.ActualClock;
import org.openzal.zal.lib.Clock;

import javax.net.ssl.SSLContext;

public class OpenChatModule extends AbstractModule
{
  public static final String MODULE_NAME = "ZimbraChat";
  private final Zimbra mZimbra;

  public OpenChatModule(
    Zimbra zimbra
  )
  {
    mZimbra = zimbra;
  }

  @Override
  protected void configure()
  {
    bind(Zimbra.class).toInstance(mZimbra);
    bind(MailboxManager.class).toInstance(mZimbra.getMailboxManager());
    bind(Provisioning.class).toInstance(new ProvisioningCache(
      mZimbra.getProvisioning(),
      new ActualClock()
    ));
    bind(ZimbraDatabase.ConnectionProvider.class).to(ZimbraConnectionProvider.class);
    bind(ChatDbHandler.class).to(ChatMariaDbHandler.class);
    bind(Clock.class).to(ActualClock.class);
    bind(ChatProperties.class).to(LdapChatProperties.class);
    bind(StatementsFactory.class).to(OpenStatementsFactory.class);
    bind(EventInterceptorFactory.class).to(UserEventInterceptorFactory.class);
    bind(SoapEncoderFactory.class).to(SoapEncoderFactoryImpl.class);
    bind(QueryArchiveInterceptorFactory.class).to(QueryArchiveInterceptorFactoryImpl.class);
    bind(UserProvider.class).to(OpenUserProvider.class);
    bind(UserModifier.class).to(OpenUserModifier.class);
    bind(StanzaRecognizer.class).to(StanzaRecognizerImpl.class);
    bind(XmppEventFactory.class).to(XmppEventFactoryImpl.class);
    bind(LocalXmppConnectionProvider.class).to(LocalXmppConnectionProviderImpl.class);
    bind(XmppEncoderFactory.class).to(XmppEncoderFactoryImpl.class);
    bind(XmppFilterOut.class).to(XmppFilterOutImpl.class);
    bind(XmppHandlerFactory.class).to(XmppHandlerFactoryImpl.class);
    bind(StanzaWriterFactory.class).to(StanzaWriterFactoryImpl.class);
    bind(SSLContext.class).toProvider(ZimbraSSLContextProvider.class);
    bind(RelationshipProvider.class).to(RelationshipProviderCombiner.class);
    bind(RelationshipModifier.class).to(RelationshipModifierProxy.class);
    bind(EventQueueFilterEvent.class).to(EventQueueFilterEventImpl.class);
    bind(SoapFilter.class).to(SoapFilterImpl.class);
    bind(XmppFilter.class).to(XmppFilterImpl.class);
    bind(CommonSessionEventFilter.class).to(CommonSessionEventFilterImpl.class);
    bind(ChatVersion.class).to(OpenchatVersion.class);
    bind(AddressResolver.class).to(AddressResolverStub.class);
    bind(UserCapabilitiesProvider.class).to(UserCapabilitiesProviderImpl.class);
    bind(HistoryMessageBuilder.class).to(HistoryMessageBuilderImpl.class);
    install(new FactoryModuleBuilder().build(SoapHandlerCreatorFactory.class));
    install(new FactoryModuleBuilder().build(InitialSoapRequestHandlerFactory.class));
    install(new FactoryModuleBuilder().build(SoapSessionFactory.class));
    install(new FactoryModuleBuilder().build(EventQueueFactory.class));
    install(new FactoryModuleBuilder().build(QueryArchiveFactory.class));
    install(new FactoryModuleBuilder().build(LastMessageInfoOperationFactory.class));
    install(new FactoryModuleBuilder()
      .implement(Parser.class,SoapParser.class)
      .build(ParserFactory.class));
    bind(RoomServerHostSetProvider.class).to(StubRoomServerHostSetProvider.class);
  }
}
