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

package com.zextras.modules.chat.server.xmpp;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.xmpp.handlers.*;
import com.zextras.modules.chat.server.xmpp.netty.StanzaProcessor;
import org.openzal.zal.AuthProvider;
import org.openzal.zal.Provisioning;

import javax.xml.stream.XMLStreamException;

@Singleton
public class XmppHandlerFactoryImpl implements XmppHandlerFactory
{
  private final StanzaRecognizer    mStanzaRecognizer;
  private final Provisioning        mProvisioning;
  private final StanzaWriterFactory mStanzaWriterFactory;
  private final UserProvider        mOpenUserProvider;
  private final AuthProvider        mAuthProvider;
  private final XmppFilterOut mXmppFilterOut;
  private final XmppEventFilter mXmppEventFilter;

  @Inject
  public XmppHandlerFactoryImpl(
    StanzaRecognizer stanzaRecognizer,
    Provisioning provisioning,
    UserProvider openUserProvider,
    StanzaWriterFactory stanzaWriterFactory,
    AuthProvider authProvider,
    XmppFilterOut xmppFilterOut,
    XmppEventFilter xmppEventFilter
  )
  {
    mStanzaRecognizer = stanzaRecognizer;
    mProvisioning = provisioning;
    mStanzaWriterFactory = stanzaWriterFactory;
    mOpenUserProvider = openUserProvider;
    mAuthProvider = authProvider;
    mXmppFilterOut = xmppFilterOut;
    mXmppEventFilter = xmppEventFilter;
  }

  public StanzaHandler createHandler(
    StanzaRecognizer.StanzaType type,
    StanzaProcessor.XmppConnectionHandler connectionStatus,
    String xml
  ) throws UnkownStanza, XMLStreamException
  {
    StanzaHandler handler;

    switch (type)
    {
      case Stream:
      {
        handler = new StreamHandler(mProvisioning, connectionStatus, mStanzaWriterFactory);
        break;
      }

      case StatusProbe:
      {
        handler = new StatusProbeHandler();
        break;
      }

      case IQAuth:
      {
        handler = new IQAuthHandler(
          connectionStatus,
          mProvisioning,
          mXmppFilterOut,
          mXmppEventFilter
        );
        break;
      }

      case StartTLS:
      {
        handler = new StartTLSHandler(connectionStatus);
        break;
      }

      case SASLAuth:
      {
        handler = new SASLAuthHandler(
          connectionStatus,
          mProvisioning,
          mOpenUserProvider,
          mXmppEventFilter,
          mXmppFilterOut
        );
        break;
      }

      case Bind:
      {
        handler = new BindHandler(connectionStatus.getSession());
        break;
      }

      case Session:
      {
        handler = new SessionHandler(connectionStatus, mProvisioning, mStanzaWriterFactory);
        break;
      }

      case Discovery:
      {
        handler = new DiscoveryHandler(connectionStatus.getSession());
        break;
      }

      case IQRoster:
      {
        handler = new IQRosterHandler(connectionStatus.getSession(),mProvisioning);
        break;
      }

      case Privacy:
      {
        handler = new IQPrivacyHandler(connectionStatus.getSession());
        break;
      }

      case Presence:
      {
        handler = new PresenceHandler(connectionStatus, mProvisioning);
        break;
      }

      case Message:
      {
        handler = new MessageHandler(connectionStatus.getSession());
        break;
      }

      case Ping:
      {
        handler = new PingHandler(connectionStatus.getSession());
        break;
      }

      case MessageAck:
      {
        handler = new MessageAckHandler(connectionStatus.getSession());
        break;
      }

      case LastActivity:
      {
        handler = new StubStanzaHandler();
        break;
      }

      case ProxyAuth:
      {
        handler = new ProxyAuthHandler(
          connectionStatus,
          mProvisioning,
          mAuthProvider,
          mXmppEventFilter,
          mXmppFilterOut
        );
        break;
      }

      case UnknownIq:
        handler = new FeatureNotImplementedHandler(connectionStatus);
        break;

      case Unknown:
        throw new UnkownStanza(xml);

      default:
        throw new UnkownStanza("unhandled stanza type "+type.toString());
    }

    return handler;
  }

  @Override
  public StanzaHandler createHandler(
    StanzaProcessor.XmppConnectionHandler connectionStatus,
    String xml
  ) throws UnkownStanza, XMLStreamException
  {
    StanzaRecognizer.StanzaType type = mStanzaRecognizer.recognize(xml);

    return createHandler(type, connectionStatus, xml);
  }
}
