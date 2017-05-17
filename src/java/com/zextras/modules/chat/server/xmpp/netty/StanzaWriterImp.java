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

package com.zextras.modules.chat.server.xmpp.netty;

import com.zextras.lib.log.ChatLog;
import com.zextras.lib.log.CurrentLogContext;
import com.zextras.lib.log.LogContext;
import com.zextras.modules.chat.server.events.EventInterceptorFactory;
import com.zextras.modules.chat.server.events.EventManager;
import com.zextras.modules.chat.server.events.EventQueue;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.interceptors.EventInterceptor;
import com.zextras.modules.chat.server.xmpp.XmppEventInterceptorFactory;
import com.zextras.modules.chat.server.xmpp.encoders.XmppEncoder;
import com.zextras.modules.chat.server.xmpp.encoders.XmppEncoderFactory;
import org.openzal.zal.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.io.ByteArrayOutputStream;

public class StanzaWriterImp implements StanzaWriter
{
  private final XmppEncoderFactory                    mEncoderFactory;
  private final StanzaProcessor.XmppConnectionHandler mXmppConnectionHandler;
  private final EventManager                          mEventManager;
  private final XmppEventInterceptorFactory               mXmppEventInterceptorFactory;

  public StanzaWriterImp(
    XmppEncoderFactory encoderFactory,
    StanzaProcessor.XmppConnectionHandler xmppConnectionHandler,
    EventManager eventManager,
    XmppEventInterceptorFactory xmppEventInterceptorFactory
  )
  {
    mEncoderFactory = encoderFactory;
    mXmppConnectionHandler = xmppConnectionHandler;
    mEventManager = eventManager;
    mXmppEventInterceptorFactory = xmppEventInterceptorFactory;
  }

  @Override
  public void onAttached(EventQueue eventQueue) {
  }

  @Override
  public void onDetached(EventQueue eventQueue) {
  }

  @Override
  public void onEventQueued(EventQueue eventQueue, Event queuedEvent){
  }

  @Override
  public void onEventQueued(EventQueue eventQueue)
  {
    try
    {
      final ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
      for (Event event : eventQueue.popAllEvents())
      {
        LogContext logContext = CurrentLogContext.begin();
        if (event.getTarget().getAddresses().size() == 1)
        {
          logContext.setAccountName(event.getTarget().toSingleAddress());
        }
        logContext.freeze();
        try
        {
          out.reset();
          XmppEncoder encoder = (XmppEncoder) event.interpret(mEncoderFactory);
          if (encoder == null)
          {
            ChatLog.log.debug("No encoder found for event " + event.getClass().getName());
            continue;
          }

          final SpecificAddress exposedAddress = mXmppConnectionHandler.getSession().getExposedAddress();

          encoder.encode(out, exposedAddress);

          String stanzaDebug = new String(out.toByteArray(), "UTF-8");
          ChatLog.log.debug("writing: " + event.getClass().getName());
          ChatLog.log.debug("writing stanza(" + stanzaDebug.length() + "): " + stanzaDebug);
          ByteBuf stanza = Unpooled.copiedBuffer(out.toByteArray());
          ChannelFuture writeFuture = mXmppConnectionHandler.write(stanza);

          final Event eventToNotify = event;

          writeFuture.addListener(new ChannelFutureListener()
          {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception
            {
              if (future.isSuccess())
              {
                EventInterceptor interceptor = eventToNotify.interpret(mXmppEventInterceptorFactory);
                interceptor.intercept(mEventManager, exposedAddress);
              }
            }
          });
        }
        finally
        {
          CurrentLogContext.end();
        }
      }
    }
    catch (Throwable ex)
    {
      ChatLog.log.warn("Exception: " + Utils.exceptionToString(ex));
    }
  }

  @Override
  public void onEventPopped(EventQueue eventQueue, Event poppedEvent) {
  }

  @Override
  public void onQueueFlushed(EventQueue eventQueue) {
  }

  @Override
  public void onEventRemoved(EventQueue eventQueue, Event event){
  }
}
