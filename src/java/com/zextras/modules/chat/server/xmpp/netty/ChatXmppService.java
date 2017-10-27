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

package com.zextras.modules.chat.server.xmpp.netty;

import com.google.inject.Singleton;
import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.properties.ChatProperties;
import com.zextras.modules.chat.server.events.EventQueueFactory;
import com.zextras.modules.chat.server.xmpp.XmppEventFilter;
import com.zextras.modules.chat.server.xmpp.XmppFilterOut;
import com.zextras.modules.core.services.NettyService;
import com.zextras.lib.ZimbraSSLContextProvider;
import com.zextras.lib.switches.Service;
import com.zextras.lib.switches.ServiceName;
import com.zextras.modules.chat.server.events.EventManager;
import com.zextras.modules.chat.server.xmpp.XmppHandlerFactory;
import com.zextras.modules.chat.server.xmpp.encoders.ProxyAuthRequestEncoder;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import io.netty.util.concurrent.DefaultProgressivePromise;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.Promise;
import org.openzal.zal.Provisioning;
import org.openzal.zal.Utils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslHandler;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.net.BindException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Singleton
public class ChatXmppService implements Runnable, Service
{
  public static final ServiceName XMPP_SERVICE_NAME      = new ServiceName("xmpp");

  private final EventManager                         mEventManager;
  private final XmppHandlerFactory                   mXmppHandlerFactory;
  private final SchemaProvider                       mSchemaProvider;
  private final ZimbraSSLContextProvider             mZimbraSSLContextProvider;
  private final ChatProperties                       mChatProperties;
  private final NettyService                         mNettyService;
  private final ProxyAuthRequestEncoder              mProxyAuthRequestEncoder;
  private final XmppFilterOut                        mXmppFilterOut;
  private final XmppEventFilter                      mXmppEventFilter;
  private final Provisioning                         mProvisioning;
  private final EventQueueFactory                    mEventQueueFactory;
  private       boolean                              mStopped;
  private       Promise<Boolean>                     mInitializationPromise;
  private final ReentrantLock    mLock      = new ReentrantLock();
  private final Condition        mCondition = mLock.newCondition();
  private       Thread           mThread    = null;

  @Inject
  public ChatXmppService(
    EventManager eventManager,
    XmppHandlerFactory xmppHandlerFactory,
    SchemaProvider schemaProvider,
    ZimbraSSLContextProvider zimbraSSLContextProvider,
    ChatProperties chatProperties,
    NettyService nettyService,
    ProxyAuthRequestEncoder proxyAuthRequestEncoder,
    XmppFilterOut xmppFilterOut,
    XmppEventFilter xmppEventFilter,
    Provisioning provisioning,
    EventQueueFactory eventQueueFactory
  )
  {
    mEventManager = eventManager;
    mXmppHandlerFactory = xmppHandlerFactory;
    mSchemaProvider = schemaProvider;
    mZimbraSSLContextProvider = zimbraSSLContextProvider;
    mChatProperties = chatProperties;
    mNettyService = nettyService;
    mProxyAuthRequestEncoder = proxyAuthRequestEncoder;
    mXmppFilterOut = xmppFilterOut;
    mXmppEventFilter = xmppEventFilter;
    mProvisioning = provisioning;
    mEventQueueFactory = eventQueueFactory;
    mStopped = false;
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public void start() throws ServiceStartException
  {
    mInitializationPromise = new DefaultProgressivePromise<Boolean>(ImmediateEventExecutor.INSTANCE);
    mThread = new Thread(this);
    mThread.start();

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
    catch (BindException ex)
    {
      throw new Service.ServiceStartException("Cannot bind to ports 5222 and 5223: please make sure that no other process is bound to such ports in order to use any ZeXtras Chat XMPP feature.", ex);
    }
    catch (Throwable ex)
    {
      throw new Service.ServiceStartException(ex.getMessage(), ex);
    }
  }

  @Override
  public void stop()
  {
    shutdown();
  }

  public void run()
  {
    EventLoopGroup acceptorGroup = new NioEventLoopGroup(4);
    EventLoopGroup channelWorkerGroup = new NioEventLoopGroup(8);

    final boolean useLegacySSLPort = mChatProperties.isChatXmppSslPortEnabled();

    String logListening = "XMPP listening on ports 5222";
    if( useLegacySSLPort ) {
      logListening += ", 5223";
    }
    ChatLog.log.info(logListening);

    try
    {
      SSLContext zimbraSSLContext = mZimbraSSLContextProvider.get();
      ServerBootstrap bootstrapTLS = buildBoostrap(acceptorGroup, channelWorkerGroup, zimbraSSLContext, false);
      ServerBootstrap bootstrapOldSSL = buildBoostrap(acceptorGroup, channelWorkerGroup, zimbraSSLContext, true);

      ChannelFuture futureTLS = bootstrapTLS.bind(mChatProperties.getChatXmppPort(mProvisioning.getLocalServer().getName())).sync(); //addListener
      ChannelFuture futureOldSSL = null;
      if( useLegacySSLPort ) {
        futureOldSSL = bootstrapOldSSL.bind(mChatProperties.getChatXmppSslPort(mProvisioning.getLocalServer().getName())).sync();
      }

      ChatLog.log.info("XMPP started");

      mInitializationPromise.setSuccess(true);

      mLock.lock();
      try
      {
        while (!mStopped)
        {
          try {
            mCondition.await();
          }
          catch (InterruptedException ex) {
          }
        }

        mStopped = false;
      }
      finally {
        mLock.unlock();
      }

      ChatLog.log.info("XMPP shutting down");

      if( useLegacySSLPort ) {
        futureOldSSL.channel().close().sync();
      }
      futureTLS.channel().close().sync();

    }
    catch (Throwable ex)
    {
      ChatLog.log.warn(Utils.exceptionToString(ex));
      mInitializationPromise.setFailure(ex);
    }
    finally
    {
      acceptorGroup.shutdownGracefully();
      channelWorkerGroup.shutdownGracefully();
    }
  }

  public void shutdown()
  {
    Thread thread;
    mLock.lock();
    try
    {
      mStopped = true;
      mCondition.signalAll();
      thread = mThread;
      mThread = null;
    }
    finally {
      mLock.unlock();
    }

    try {
      if( thread != null ) {
        thread.join();
      }
    }
    catch (InterruptedException e){
    }
  }

  private ServerBootstrap buildBoostrap(
    EventLoopGroup acceptorGroup,
    EventLoopGroup workerGroup,
    final SSLContext zimbraSSLContext,
    final boolean oldSSL
  )
  {

    ServerBootstrap serverBootstrap = new ServerBootstrap();
    serverBootstrap.group(acceptorGroup, workerGroup);

    serverBootstrap.channel(NioServerSocketChannel.class);

    ChannelHandler handler = new ChannelInitializer<SocketChannel>(){
      @Override
      public void initChannel(SocketChannel ch) throws Exception
      {
        try
        {
          if (oldSSL) {
            final SSLEngine engine = zimbraSSLContext.createSSLEngine();
            engine.setUseClientMode(false);
            ch.pipeline().addFirst(null, "SSL", new SslHandler(engine, false));
          }

          ch.pipeline().addLast(null, "SubTagTokenizer", new XmlSubTagTokenizer());
          FirstTags firstTagsHandler = new FirstTags(
            mXmppHandlerFactory,
            mEventManager,
            ch,
            mSchemaProvider,
            zimbraSSLContext,
            oldSSL,
            mChatProperties,
            mNettyService,
            mProxyAuthRequestEncoder,
            mXmppEventFilter,
            mXmppFilterOut,
            mEventQueueFactory
          );
          ch.pipeline().addAfter("SubTagTokenizer", "FirstTags", firstTagsHandler);
        }
        catch ( Throwable ex )
        {
          ChatLog.log.warn("Unable to initialize XMPP connection: "+ Utils.exceptionToString(ex));
          ch.close();
        }
      }
    };

    serverBootstrap
      .childHandler(handler)
      .option(ChannelOption.SO_BACKLOG, 128)
      .childOption(ChannelOption.SO_KEEPALIVE, true)
      .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 0);

    return serverBootstrap;
  }
}
