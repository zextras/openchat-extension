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

import com.zextras.lib.activities.ActivityManager;
import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.destinations.LocalServerDestination;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import org.openzal.zal.Utils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.charset.Charset;

public class EventSenderImpl implements EventSender, Runnable
{
  private final    LocalXmppConnectionProvider mLocalXmppConnectionProvider;
  private final    DestinationQueue            mDestinationQueue;
  private final    int                         mSteps;
  private final    String                      mHost;
  private final    ActivityManager             mActivityManager;
  private          Channel                     mChannel;
  private volatile boolean                     mRequestStop;


  public EventSenderImpl(
    ActivityManager activityManager,
    LocalXmppConnectionProvider localXmppConnectionProvider,
    DestinationQueue destinationQueue,
    int steps
  )
  {
    mActivityManager = activityManager;
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
    Thread.currentThread().setName("EventSender " + mHost);
    int c = 0;
    while ((mSteps == 0 || c < mSteps) && !mRequestStop)
    {
      try
      {
        c++;
        QueuedEvent queuedEvent;
        try
        {
          queuedEvent = mDestinationQueue.getQueuedEvent();
        }
        catch (InterruptedException e)
        {
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
      catch (Throwable t)
      {
        ChatLog.log.crit(Utils.exceptionToString(t));
      }
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
    try
    {
      stanza = queuedEvent.encodeToXmpp();
    }
    catch (XMLStreamException e)
    {
      ChatLog.log.err(e.getMessage());
      return;
    }

    for (int i = 0; !mRequestStop && i < 10; i++)
    {
      try
      {
        tryDelivery(stanza);
        success(queuedEvent);
        return;
      }
      catch(IOException e)
      {
        try
        {
          Thread.sleep(2^i * 1000);
        }
        catch (InterruptedException ignore)
        {}
      }
      catch(Throwable e)
      {
        ChatLog.log.err("Sending event " + queuedEvent.getEvent().getClass() + " to " + mHost + " error: " + Utils.exceptionToString(e));
        return;
      }
    }
  }

  @Override
  public void saveOnDisk(QueuedEvent queuedEvent) {
    ChatLog.log.err("EventSender: failed to send event : " + queuedEvent.getEvent().getClass().getName()
      + " to " + queuedEvent.getRecipient().resourceAddress());
  }

  @Override
  public void success(QueuedEvent queuedEvent) {
  }

  @Override
  public void start()
  {
    mActivityManager.addActivity(this);
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
    mRequestStop = true;
  }
}
