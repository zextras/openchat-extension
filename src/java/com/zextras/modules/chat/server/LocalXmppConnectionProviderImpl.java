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

import com.google.inject.Inject;
import com.zextras.lib.ZimbraSSLContextProvider;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;
import java.io.IOException;

public class LocalXmppConnectionProviderImpl implements LocalXmppConnectionProvider
{
  private final ZimbraSSLContextProvider mZimbraSSLContextProvider;

  @Inject
  public LocalXmppConnectionProviderImpl(ZimbraSSLContextProvider zimbraSSLContextProvider)
  {
    mZimbraSSLContextProvider = zimbraSSLContextProvider;
  }

  @Override
  public Channel openConnection(String host, int port, final ChannelHandler channelHandler) throws IOException
  {
    ChannelHandler handler = new ChannelInitializer<SocketChannel>() {
      @Override
      protected void initChannel(SocketChannel socketChannel) throws Exception
      {
        SSLEngine sslEngine = mZimbraSSLContextProvider.get().createSSLEngine();
        sslEngine.setUseClientMode(true);
        SslHandler sslHandler = new SslHandler(sslEngine);
        socketChannel.pipeline().addFirst(
          "ssl",
          sslHandler
        );
        socketChannel.pipeline().addLast("handler", channelHandler);
      }
    };
    ChannelFuture channelFuture = new Bootstrap()
      .channel(NioSocketChannel.class)
      .group(new NioEventLoopGroup())
      .handler(handler)
      .connect(host, port);

    try
    {
      channelFuture.sync();
      if (!channelFuture.isSuccess())
      {
        throw channelFuture.cause();
      }

      return channelFuture.channel();
    }
    catch (Throwable t)
    {
      throw new IOException(t);
    }
  }
}
