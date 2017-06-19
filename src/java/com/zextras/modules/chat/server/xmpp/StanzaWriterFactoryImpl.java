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
import com.zextras.modules.chat.server.events.EventManager;
import com.zextras.modules.chat.server.xmpp.encoders.XmppEncoderFactory;
import com.zextras.modules.chat.server.xmpp.encoders.XmppEncoderFactoryImpl;
import com.zextras.modules.chat.server.xmpp.netty.StanzaProcessor;
import com.zextras.modules.chat.server.xmpp.netty.StanzaWriter;
import com.zextras.modules.chat.server.xmpp.netty.StanzaWriterImp;

@Singleton
public class StanzaWriterFactoryImpl implements StanzaWriterFactory
{
  private final XmppEncoderFactory mEncoderFactory;
  private final EventManager       mEventManager;

  @Inject
  public StanzaWriterFactoryImpl(
    XmppEncoderFactory encoderFactory,
    EventManager eventManager
  )
  {
    mEncoderFactory = encoderFactory;
    mEventManager = eventManager;
  }

  @Override
  public StanzaWriter create(StanzaProcessor.XmppConnectionHandler connectionHandler)
  {
    XmppEventInterceptorFactory xmppEventInterceptorFactory = new XmppEventInterceptorFactoryImpl(connectionHandler);
    return new StanzaWriterImp(mEncoderFactory, connectionHandler, mEventManager, xmppEventInterceptorFactory);
  }
}
