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

package com.zextras.modules.chat.server.listener;

import com.zextras.lib.json.JSONObject;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.encoding.Encoder;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventInterpreter;
import com.zextras.modules.chat.server.events.EventQueue;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.response.ChatSoapResponse;
import com.zextras.modules.chat.server.soap.SoapEncoder;
import com.zextras.modules.chat.server.soap.encoders.SoapEncoderFactory;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.openzal.zal.soap.SoapResponse;


public class PingWebSocketQueueListener implements PingQueueListener
{
  private final SpecificAddress mAddress;
  private final SoapEncoderFactory mEncoderFactory;
  private final Channel mChannel;


  public PingWebSocketQueueListener(
    SoapEncoderFactory encoderFactory,
    Channel channel,
    SpecificAddress address
  )
  {
    mEncoderFactory = encoderFactory;
    mChannel = channel;
    mAddress = address;
  }

  @Override
  public void onAttached(EventQueue eventQueue)
  {
  }

  @Override
  public void onDetached(EventQueue eventQueue)
  {
  }

  @Override
  public void onEventQueued(EventQueue eventQueue, Event queuedEvent)
  {
    ChatSoapResponse chatSoapResponse = new ChatSoapResponse();
    try
    {
      SoapEncoder encoder = (SoapEncoder) queuedEvent.interpret(mEncoderFactory);
      encoder.encode(chatSoapResponse, mAddress);
    }
    catch(ChatException e)
    {
      throw new RuntimeException(e);
    }

    JSONObject response = new JSONObject();
    response.put("responses", chatSoapResponse.getJson().toString());
    mChannel.writeAndFlush(new TextWebSocketFrame(response.toString()));

    eventQueue.popAllEvents();
  }

  @Override
  public void onEventQueued(EventQueue eventQueue)
  {
    JSONObject response = new JSONObject();
    ChatSoapResponse chatSoapResponse = new ChatSoapResponse();
    for(Event event : eventQueue.peekAllEvents())
    {
      try
      {
        SoapEncoder encoder = (SoapEncoder) event.interpret(mEncoderFactory);
        encoder.encode(chatSoapResponse, mAddress);
      }
      catch(ChatException e)
      {
        throw new RuntimeException(e);
      }
    }
    response.put("responses", chatSoapResponse.getJson().toString());
    mChannel.writeAndFlush(new TextWebSocketFrame(response.toString()));
    eventQueue.popAllEvents();
  }

  @Override
  public void suspendContinuation(
    Object continuationObject,
    long timeoutInMs
  )
  {

  }

  @Override
  public void onEventPopped(EventQueue eventQueue, Event poppedEvent)
  {
  }

  @Override
  public void onQueueFlushed(EventQueue eventQueue)
  {
  }

  @Override
  public void onEventRemoved(EventQueue eventQueue, Event event)
  {
  }

  @Override
  public boolean alreadyReplied()
  {
    return true;
  }

  public void encodeEvents(SoapResponse response, EventInterpreter<Encoder> encoderFactory)
  {}

  @Override
  public String toString()
  {
    return "PingWebSocketQueueListener{" +
      ", mAddress=" + mAddress +
      ", HashCode=" + hashCode() +
      '}';
  }
}
