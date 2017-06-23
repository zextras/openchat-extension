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

package com.zextras.modules.chat.server;

import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.destinations.LocalServerDestination;
import com.zextras.modules.core.netty.EventLoopGroupProvider;
import com.zextras.modules.core.services.NettyService;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import org.openzal.zal.Provisioning;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;

public class EventSenderImpl implements EventSender, Runnable
{
  private final Provisioning                mProvisioning;
  private final LocalXmppConnectionProvider mLocalXmppConnectionProvider;
  private final DestinationQueue            mDestinationQueue;
  private final int mSteps;
  private final String                      mHost;
  private       Channel                     mChannel;
  private       boolean                     mRequestStop;
  private       Thread                      mThread;

  public EventSenderImpl(
    Provisioning provisioning,
    LocalXmppConnectionProvider localXmppConnectionProvider,
    DestinationQueue destinationQueue,
    int steps
  )
  {
    mProvisioning = provisioning;
    mLocalXmppConnectionProvider = localXmppConnectionProvider;
    mDestinationQueue = destinationQueue;
    mSteps = steps;
    mHost = mDestinationQueue.getHost();
    mRequestStop = false;
    mChannel = null;
  }

  @Override
  public void run()
  {
    int c = 0;
    while ((mSteps == 0 || c < mSteps) && !mRequestStop)
    {
      c++;
      QueuedEvent queuedEvent;
      try {
        queuedEvent = mDestinationQueue.getQueuedEvent();
      } catch (InterruptedException e) {
        continue;
      }
      if (queuedEvent.getNextRetry() > System.currentTimeMillis())
      {
        mDestinationQueue.addEvent(queuedEvent);
        continue;
      }
      ChatLog.log.debug("EventSender: deliverEvent: " + queuedEvent.getEvent().getClass().getName()
        + " to " + queuedEvent.getRecipient().resourceAddress());
      sendEvent(queuedEvent);
    }
  }

  private void authConnection(Socket connection)
  {
    // TODO!
    if (
      mProvisioning.getServerByName(
        ((InetSocketAddress) connection.getRemoteSocketAddress()).getAddress().getHostName()
      ) == null
    )
    {
      throw new RuntimeException();
    }
  }

  private Channel getChannel() throws IOException
  {
    if (mChannel == null || !mChannel.isOpen())
    {
      mChannel = mLocalXmppConnectionProvider.openConnection(
        mHost,
        LocalServerDestination.DEFAULT_LOCAL_XMPP_PORT,
        new ChannelOutboundHandlerAdapter()
      );
    }

    return mChannel;
  }

  public void tryDelivery(String stanza) throws Throwable
  {
    Channel channel = getChannel();
    ChannelFuture channelFuture = channel.writeAndFlush(
      Unpooled.wrappedBuffer(stanza.getBytes(Charset.defaultCharset()))
    ).sync();
    if (!channelFuture.isSuccess())
    {
      throw channelFuture.cause();
    }
  }

  @Override
  public void sendEvent(QueuedEvent queuedEvent)
  {
    queuedEvent.updateRetry();
    String stanza;
    try {
      stanza = queuedEvent.encodeToXmpp();
    } catch (XMLStreamException e) {
      ChatLog.log.err(e.getMessage());
      return;
    }

    try
    {
      tryDelivery(stanza);
      success(queuedEvent);
    }
    catch (Throwable e)
    {
      if (queuedEvent.getRetryCount() >= 15)
      {
        saveOnDisk(queuedEvent);
      }
      mDestinationQueue.addEvent(queuedEvent);
      try
      {
        Thread.sleep( 1000L );
      }
      catch (InterruptedException ignore){
      }
    }
  }

  @Override
  public void saveOnDisk(QueuedEvent queuedEvent) {
    //TODO
  }

  @Override
  public void success(QueuedEvent queuedEvent) {
    //TODO
  }

  @Override
  public void start()
  {
    if ( mThread != null )
    {
      throw new RuntimeException("Invalid Thread state");
    }
    mRequestStop = false;
    mThread = new Thread(this);
    mThread.start();
  }

  @Override
  public void stop() throws InterruptedException
  {
    if ( mChannel != null && mChannel.isOpen())
    {
      ChannelFuture future = mChannel.close().sync();
      if (!future.isSuccess())
      {
        throw new RuntimeException(future.cause());
      }
    }
    if ( mThread == null )
    {
      throw new RuntimeException("Invalid Thread state");
    }
    mRequestStop = true;
    mThread.interrupt();
    mThread = null;
  }
}
