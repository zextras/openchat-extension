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

import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.operations.SendXmppFeature;
import com.zextras.modules.chat.server.operations.XmppSASLAuthentication;
import com.zextras.modules.chat.server.session.CommonSessionEventInterceptorBuilder;
import com.zextras.modules.chat.server.xmpp.StanzaHandler;
import com.zextras.modules.chat.server.xmpp.XmppEventFilter;
import com.zextras.modules.chat.server.xmpp.XmppFilterOut;
import com.zextras.modules.chat.server.xmpp.netty.StanzaProcessor;
import com.zextras.modules.chat.server.xmpp.parsers.SASLAuthParser;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.openzal.zal.Provisioning;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

public class SASLAuthHandler implements StanzaHandler {
  private final CommonSessionEventInterceptorBuilder mCommonSessionEventInterceptorBuilder;
  private final StanzaProcessor.XmppConnectionHandler mXmppConnectionHandler;
  private final Provisioning                          mProvisioning;
  private final UserProvider                          mOpenUserProvider;
  private final XmppEventFilter mXmppEventFilter;
  private final XmppFilterOut mXmppFilterOut;
  private       SASLAuthParser                        mParser;

  public SASLAuthHandler(
    CommonSessionEventInterceptorBuilder commonSessionEventInterceptorBuilder,
    StanzaProcessor.XmppConnectionHandler xmppConnectionStatus,
    Provisioning provisioning,
    UserProvider openUserProvider,
    XmppEventFilter xmppEventFilter,
    XmppFilterOut xmppFilterOut
  )
  {
    mCommonSessionEventInterceptorBuilder = commonSessionEventInterceptorBuilder;
    mXmppConnectionHandler = xmppConnectionStatus;
    mProvisioning = provisioning;
    mOpenUserProvider = openUserProvider;
    mXmppEventFilter = xmppEventFilter;
    mXmppFilterOut = xmppFilterOut;
  }

  @Override
  public List<ChatOperation> handle()
  {

    XmppSASLAuthentication saslAuthEvent = new XmppSASLAuthentication(
      mCommonSessionEventInterceptorBuilder,
      mProvisioning,
      mParser,
      mXmppConnectionHandler,
      mOpenUserProvider,
      mXmppEventFilter,
      mXmppFilterOut
    );

    SendXmppFeature featureEvent = new SendXmppFeature(
      mXmppConnectionHandler
    );

    return Arrays.<ChatOperation>asList(saslAuthEvent, featureEvent);
  }

  @Override
  public void parse(ByteArrayInputStream xmlInputStream, SchemaProvider schemaProvider) throws XMLStreamException {
    mParser = new SASLAuthParser(xmlInputStream, schemaProvider);
    mParser.parse();
  }
}
