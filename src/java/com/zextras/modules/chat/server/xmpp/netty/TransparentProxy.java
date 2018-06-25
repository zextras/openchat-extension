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

import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.properties.ChatProperties;
import com.zextras.modules.core.services.NettyService;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultProgressivePromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.Promise;
import org.openzal.zal.Account;
import org.openzal.zal.Utils;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;


/*
          mClientChannel                 mServerChannel
  CLIENT <==============> PUBLIC SERVER <==============> PRIVATE SERVER

*/

public class TransparentProxy
{
  private static final String STREAM_INIT = "<stream:stream xmlns:zextras='http://zextras.com/xmpp' zextras:proxy='true' to='_DOMAIN_' xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' version='1.0'>"; //#ZextrasRef
  private final Account      mAccount;
  private final String       mInitialPayload;
  private final int          mPort;
  private final Channel      mClientChannel;
  private final NettyService mNettyService;
  public        Channel      mServerChannel;
  private final String       mStreamInit;

  public TransparentProxy(
    ChatProperties chatProperties,
    NettyService nettyService,
    Account account,
    Channel clientChannel,
    String initialPayload,
    Channel proxyChannel
  )
  {
    mNettyService = nettyService;
    mAccount = account;
    mInitialPayload = initialPayload;
    mPort = chatProperties.getChatXmppPort(account.getMailHost());
    mClientChannel = clientChannel;
    mServerChannel = proxyChannel;
    String domain = account.getDomainId();
    String streamInit = STREAM_INIT;
    if (domain != null)
    {
      streamInit = streamInit.replace("_DOMAIN_", account.getDomainName());
    }
    mStreamInit = streamInit;
  }

  public TransparentProxy(
    ChatProperties chatProperties,
    NettyService nettyService,
    Account account,
    Channel clientChannel,
    String initialPayload
  )
  {
    mNettyService = nettyService;
    mAccount = account;
    mInitialPayload = initialPayload;
    mPort = chatProperties.getChatXmppPort(account.getMailHost());
    mClientChannel = clientChannel;
    mServerChannel = null;
    mStreamInit = STREAM_INIT.replace("_DOMAIN_", account.getDomainName());
  }

  public Future<Channel> connect()
  {
    final Promise<Channel> channelFuture = new DefaultProgressivePromise<Channel>(ImmediateEventExecutor.INSTANCE);

    if (mServerChannel == null)
    {
      Bootstrap bootstrap = new Bootstrap();
      bootstrap.group(mNettyService.getEventLoopGroup())
        .channel(NioSocketChannel.class)
        .remoteAddress(new InetSocketAddress(mAccount.getMailHost(), mPort))
        .handler(new Initializer());

      ChannelFuture serverChannelFuture = bootstrap.connect();
      serverChannelFuture.addListener(
        new ChannelFutureListener()
        {
          @Override
          public void operationComplete(ChannelFuture future)
          {
            if (future.isSuccess())
            {
              ChatLog.log.info("Proxy xmpp requests for " + mAccount.getName() + " to " + mAccount.getMailHost());
              mServerChannel = future.channel();

              mServerChannel.write(Unpooled.wrappedBuffer(mStreamInit.getBytes()));
              mServerChannel.writeAndFlush(Unpooled.wrappedBuffer(mInitialPayload.getBytes()));

              mServerChannel.pipeline().addLast(
                "proxyToClient",
                new Proxy(mClientChannel)
              );

              mClientChannel.pipeline().addLast(
                "proxyToServer",
                new Proxy(mServerChannel)
              );

              mServerChannel.closeFuture().addListener(
                new ChannelFutureListener()
                {
                  @Override
                  public void operationComplete(ChannelFuture future) throws Exception
                  {
                    mClientChannel.close();
                  }
                });

              mClientChannel.closeFuture().addListener(
                new ChannelFutureListener()
                {
                  @Override
                  public void operationComplete(ChannelFuture future) throws Exception
                  {
                    mServerChannel.close();
                  }
                }
              );

              future.channel().closeFuture();

              channelFuture.setSuccess(mServerChannel);
            }
            else
            {
              ChatLog.log.info("Cannot proxy xmpp requests for " + mAccount.getName() + " to " + mAccount.getMailHost() +
                ": " + Utils.exceptionToString(future.cause()));
              sendInternalError(mClientChannel);
              mClientChannel.flush().close();
              channelFuture.setFailure(future.cause());
            }
          }
        }
      );

      return channelFuture;
    }
    else
    {
      mServerChannel.pipeline().addLast(
        "proxyToClient",
        new Proxy(mClientChannel)
      );

      mServerChannel.writeAndFlush(mInitialPayload.getBytes());

      channelFuture.setSuccess(mServerChannel);
      return channelFuture;
    }
  }

  private void sendInternalError(Channel clientChannel)
  {
    clientChannel.write("broken");
  }

  private class Initializer extends ChannelInitializer<Channel>
  {
    @Override
    public void initChannel(Channel ch)
    throws Exception
    {
    }
  }

  public static class Proxy extends ChannelInboundHandlerAdapter
  {
    private final Channel mDestinationChannel;
    private ByteBuf mBuffer = Unpooled.buffer();

    public Proxy(Channel destinationChannel)
    {
      mDestinationChannel = destinationChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
      mBuffer.writeBytes((ByteBuf) msg);
      writeBuffer(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx)
    {
      writeBuffer(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
      ChatLog.log.err("[RCV] Connection error from " + ctx.channel().localAddress() + " to " + ctx.channel().remoteAddress() + " connection status: " + (ctx.channel().isOpen() ? "Open":"Close") + ". Exception : " + Utils.exceptionToString(cause));

      ctx.close();
      mDestinationChannel.close();
    }

    private void writeBuffer(final ChannelHandlerContext ctx)
    {
      // TODO check mBuffer size
      if (mBuffer.readableBytes() > 0 && mDestinationChannel.isWritable())
      {
        ChatLog.log.debug("Proxy("+ctx.channel().pipeline().lastContext().name()+"): " +
                           mBuffer.toString(Charset.defaultCharset()));

        mDestinationChannel.writeAndFlush(mBuffer.readBytes(mBuffer.readableBytes()))
          .addListener(new ChannelFutureListener()
          {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception
            {
              writeComplete(future, ctx.channel());
            }
          });
      }
    }

    public void writeComplete(ChannelFuture channelFuture, Channel sourceChannel)
    {
      if (channelFuture.isSuccess())
      {
        mBuffer.discardReadBytes();
      }
      else
      {
        ChatLog.log.err(Utils.exceptionToString(channelFuture.cause()));
        sourceChannel.close();
        mDestinationChannel.close();
      }
    }
  }
}
