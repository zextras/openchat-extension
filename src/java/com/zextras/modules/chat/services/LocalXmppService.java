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

package com.zextras.modules.chat.services;

import com.google.inject.Inject;
import com.zextras.lib.ZimbraSSLContextProvider;
import com.zextras.lib.activities.ActivityManager;
import com.zextras.lib.activities.ThreadSlot;
import com.zextras.lib.log.ChatLog;
import com.zextras.lib.switches.Service;
import com.zextras.modules.chat.server.LocalXmppReceiver;
import com.zextras.modules.chat.server.SSLCipher;
import com.zextras.modules.chat.server.xmpp.netty.XmlSubTagTokenizer;
import com.zextras.modules.chat.server.xmpp.netty.XmlTagTokenizer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.DefaultProgressivePromise;
import io.netty.util.concurrent.ImmediateEventExecutor;
import org.openzal.zal.Utils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LocalXmppService implements Runnable, Service
{
  public static final int DEFAULT_LOCAL_XMPP_PORT = 5269;

  private       DefaultProgressivePromise<Void> mInitializationPromise;
  private final ReentrantLock                   mLock;
  private final ZimbraSSLContextProvider        mZimbraSSLContextProvider;
  private final ActivityManager mActivityManager;
  private final LocalXmppReceiver mLocalXmppReceiver;
  private final SSLCipher mSslCipher;
  private       boolean           mStopRequested;
  private final Condition         mWaitStopRequest;

  @Inject
  public LocalXmppService(
    ZimbraSSLContextProvider zimbraSSLContextProvider,
    ActivityManager activityManager,
    LocalXmppReceiver localXmppReceiver,
    SSLCipher sslCipher
  )
  {
    mZimbraSSLContextProvider = zimbraSSLContextProvider;
    mActivityManager = activityManager;
    mLocalXmppReceiver = localXmppReceiver;
    mSslCipher = sslCipher;
    mStopRequested = false;
    mLock = new ReentrantLock();
    mWaitStopRequest = mLock.newCondition();
  }

  @Override
  public void start() throws ServiceStartException
  {
    mInitializationPromise = new DefaultProgressivePromise<Void>(ImmediateEventExecutor.INSTANCE);
    mActivityManager.addActivity(this);
    try
    {
      while (true)
      {
        if (mInitializationPromise.isDone())
        {
          if (!mInitializationPromise.isSuccess())
          {
            throw mInitializationPromise.cause();
          }
          return;
        }
        Thread.yield();
      }
    }
    catch (Throwable ex)
    {
      throw new Service.ServiceStartException("Cannot bind to port "+DEFAULT_LOCAL_XMPP_PORT+": please make sure that no other process is bound to such port.", ex);
    }
  }

  @Override
  public void stop()
  {
    mLock.lock();
    try
    {
      mStopRequested = true;
      mWaitStopRequest.signalAll();
    }
    finally
    {
      mLock.unlock();
    }
  }

  @Override
  public void run()
  {
    ChatLog.log.info("Listening on port " + DEFAULT_LOCAL_XMPP_PORT);
    EventLoopGroup acceptorGroup = new NioEventLoopGroup(4);
    EventLoopGroup channelWorkerGroup = new NioEventLoopGroup(8);

    Channel channel;
    try
    {
      ServerBootstrap bootstrap = new ServerBootstrap();
      bootstrap.group(acceptorGroup, channelWorkerGroup);
      bootstrap.channel(NioServerSocketChannel.class);
      final SSLContext sslContext = mZimbraSSLContextProvider.get();
      ChannelHandler handler = new ChannelInitializer<SocketChannel>() {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception
        {
          try
          {
            final SSLEngine sslEngine = sslContext.createSSLEngine();
            sslEngine.setUseClientMode(false);
            mSslCipher.setCiphers(sslContext,sslEngine);
            SslHandler sslHandler = new SslHandler(sslEngine);
            ch.pipeline().addFirst("ssl", sslHandler);
            ch.pipeline().addLast(null, "SubTagTokenizer", new XmlSubTagTokenizer());
            ch.pipeline().addLast(null, "XmlTagTokenizer", new XmlTagTokenizer());
            ch.pipeline().addAfter("XmlTagTokenizer", "StanzaProcessor", new ChannelInboundHandlerAdapter()
            {
              @Override
              public void channelRead(ChannelHandlerContext ctx, Object msg)
              {
                mLocalXmppReceiver.processStanza((String) msg);
              }
            });
          }
          catch ( Throwable t )
          {
            ChatLog.log.warn("Unable to initializer XMPP connection: " + Utils.exceptionToString(t));
            ch.close();
          }
        }
      };

      ChannelFuture channelFuture = bootstrap.childHandler(handler)
        .option(ChannelOption.SO_BACKLOG, 128)
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 0)
        .bind(DEFAULT_LOCAL_XMPP_PORT).sync();

      if (!channelFuture.isSuccess())
      {
        throw channelFuture.cause();
      }

      channel = channelFuture.channel();
      mInitializationPromise.setSuccess(null);
    }
    catch (Throwable e)
    {
      mInitializationPromise.setFailure(e);
      return;
    }

    mLock.lock();
    try
    {
      while (!mStopRequested)
      {
        try
        {
          mWaitStopRequest.await();
        }
        catch (InterruptedException ignored) {}
      }

      channel.close().sync();

      acceptorGroup.shutdownGracefully().sync();
      channelWorkerGroup.shutdownGracefully().sync();
    }
    catch (InterruptedException ignored) {}
    finally
    {
      mLock.unlock();
    }
  }
}
