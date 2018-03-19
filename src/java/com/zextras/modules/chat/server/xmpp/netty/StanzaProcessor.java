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
import com.zextras.modules.chat.server.events.EventQueue;
import com.zextras.modules.chat.server.xmpp.XmppEventFilter;
import com.zextras.modules.chat.server.xmpp.XmppFilterOut;
import com.zextras.modules.core.services.NettyService;
import com.zextras.modules.chat.server.events.EventManager;
import com.zextras.modules.chat.server.operations.ProxyAuthentication;
import com.zextras.modules.chat.server.operations.UnregisterSession;
import com.zextras.modules.chat.server.session.SessionManager;
import com.zextras.modules.chat.server.session.SessionUUID;
import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.xmpp.AnonymousXmppSession;
import com.zextras.modules.chat.server.xmpp.XmppHandlerFactory;
import com.zextras.modules.chat.server.xmpp.StanzaHandler;
import com.zextras.modules.chat.server.xmpp.XmppSession;
import com.zextras.modules.chat.server.xmpp.encoders.ProxyAuthRequestEncoder;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.jetbrains.annotations.Nullable;
import org.openzal.zal.Account;
import org.openzal.zal.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.DefaultChannelPromise;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.GenericFutureListener;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

public class StanzaProcessor extends ChannelInboundHandlerAdapter
{
  private final Channel mSocketChannel;
  private final SchemaProvider mSchemaProvider;
  private final SSLContext     mSslContext;
  private final boolean        mSsl;
  private final ChatProperties mChatProperties;

  public static class XmppConnectionHandler
  {
    private final NettyService mNettyService;
    private       Channel      mSocketChannel;
    private final EventManager mEventManager;
    private final SSLContext   mSSLContext;
    private final ChatProperties mChatProperties;
    private final ProxyAuthRequestEncoder mProxyAuthRequestEncoder;
    private final XmppEventFilter mXmppEventFilter;
    private final XmppFilterOut mXmppFilterOut;
    private boolean mNew = true;
    private XmppSession mSession;


    public XmppConnectionHandler(
      NettyService nettyService,
      Channel socketChannel,
      EventManager eventManager,
      SSLContext sslContext,
      boolean ssl,
      ChatProperties chatProperties,
      ProxyAuthRequestEncoder proxyAuthRequestEncoder,
      XmppEventFilter xmppEventFilter,
      XmppFilterOut xmppFilterOut
    )
    {
      mNettyService = nettyService;
      mSocketChannel = socketChannel;
      mEventManager = eventManager;
      mSSLContext = sslContext;
      mChatProperties = chatProperties;
      mProxyAuthRequestEncoder = proxyAuthRequestEncoder;
      mXmppEventFilter = xmppEventFilter;
      mXmppFilterOut = xmppFilterOut;
      mSession = new AnonymousXmppSession(
        SessionUUID.randomUUID(),
        new EventQueue(),
        socketChannel,
        chatProperties,
        mXmppEventFilter,
        mXmppFilterOut
      );
      mSession.setUsingSSL(ssl);
    }

    public Channel getSocketChannel()
    {
      return mSocketChannel;
    }

    public XmppSession getSession()
    {
      return mSession;
    }

    public void setSession(final XmppSession session, final SessionManager sessionManager)
    {
      mSession = session;
      mSocketChannel.closeFuture().addListener(
        new GenericFutureListener<ChannelFuture>()
        {
          @Override
          public void operationComplete(ChannelFuture future) throws Exception
          {
            ChatLog.log.info("Closed XMPP connection of user " + mSession.getExposedAddress().toString());
            mEventManager.execOperations(
              Collections.<ChatOperation>singletonList(
                new UnregisterSession(
                  mSession.getExposedAddress(),
                  mSession.getId()
                )
              )
            );
          }
        }
      );
    }

    public void close()
    {
      mSocketChannel.close();
    }

    public boolean isWritable()
    {
      return mSocketChannel.isOpen() && mSocketChannel.isWritable();
    }

    public ChannelFuture write(ByteBuf stanza)
    {
      if (mSocketChannel.isOpen())
      {
        return mSocketChannel.writeAndFlush(stanza);
      }
      else
      {
        ChatLog.log.info("Lost XMPP connection: " + mSocketChannel.toString());
        mSocketChannel.close();
        return new DefaultChannelPromise(mSocketChannel);
      }
    }

    public void startTLS(ByteBuf stanza)
    {
      mSession.setUsingSSL(true);

      SSLEngine engine = mSSLContext.createSSLEngine();
      engine.setUseClientMode(false);

      mSocketChannel.pipeline().addFirst(
        "ssl", new SslHandler(engine, true)
      );

      write(stanza);
    }

    public void transparentProxySASLAuthForAccount(Account account)
    {
      transparentProxyForAccount(account, ProxyAuthentication.AuthType.SASL, mSession.isUsingSSL(), null, null);
    }

    public void transparentProxyIQAuthForAccount(Account account, String eventId, String resource)
    {
      transparentProxyForAccount(account, ProxyAuthentication.AuthType.IQ, mSession.isUsingSSL(), eventId, resource);
    }

    private void transparentProxyForAccount(
      Account account,
      ProxyAuthentication.AuthType type,
      boolean isUsingSSL,
      @Nullable String eventId,
      @Nullable String resource
    )
    {
      mSession.setIsProxy(true);
      mSocketChannel.pipeline().remove("XmlTagTokenizer");
      mSocketChannel.pipeline().remove("SubTagTokenizer");
      mSocketChannel.pipeline().remove("StanzaProcessor");
      new TransparentProxy(
        mChatProperties,
        mNettyService,
        account,
        mSocketChannel,
        mProxyAuthRequestEncoder.buildInitialProxyPayload(account, isUsingSSL, type, eventId, resource)
      ).connect();
    }

    public void noLongerNew()
    {
      mNew = false;
    }

    public boolean isNew()
    {
      return mNew;
    }
  }

  private final NettyService                         mNettyService;
  private final ProxyAuthRequestEncoder              mProxyAuthRequestEncoder;
  private final XmppEventFilter                      mXmppEventFilter;
  private final XmppFilterOut mXmppFilterOut;
  private final XmppHandlerFactory                   mXmppHandlerFactory;
  private final EventManager                         mEventManager;
  private       XmppConnectionHandler                mXmppConnectionHandler;

  public StanzaProcessor(
    XmppHandlerFactory xmppHandlerFactory,
    EventManager eventManager,
    Channel socketChannel,
    SchemaProvider schemaProvider,
    SSLContext sslContext,
    boolean ssl,
    ChatProperties chatProperties,
    XmppConnectionHandler xmppConnectionHandler,
    NettyService nettyService,
    ProxyAuthRequestEncoder proxyAuthRequestEncoder,
    XmppEventFilter xmppEventFilter,
    XmppFilterOut xmppFilterOut
  )
  {
    mXmppHandlerFactory = xmppHandlerFactory;
    mEventManager = eventManager;
    mSocketChannel = socketChannel;
    mSchemaProvider = schemaProvider;
    mSslContext = sslContext;
    mSsl = ssl;
    mChatProperties = chatProperties;
    mXmppConnectionHandler = xmppConnectionHandler;
    mNettyService = nettyService;
    mProxyAuthRequestEncoder = proxyAuthRequestEncoder;
    mXmppEventFilter = xmppEventFilter;
    mXmppFilterOut = xmppFilterOut;
  }

  public StanzaProcessor(
    XmppHandlerFactory xmppHandlerFactory,
    EventManager eventManager,
    Channel socketChannel,
    SchemaProvider schemaProvider,
    SSLContext sslContext,
    boolean ssl,
    ChatProperties chatProperties,
    NettyService nettyService,
    ProxyAuthRequestEncoder proxyAuthRequestEncoder,
    XmppEventFilter xmppEventFilter,
    XmppFilterOut xmppFilterOut
  )
  {
    this(
      xmppHandlerFactory,
      eventManager,
      socketChannel,
      schemaProvider,
      sslContext,
      ssl,
      chatProperties,
      new XmppConnectionHandler(
        nettyService,
        socketChannel,
        eventManager,
        sslContext,
        ssl,
        chatProperties,
        proxyAuthRequestEncoder,
        xmppEventFilter,
        xmppFilterOut
      ),
      nettyService,
      proxyAuthRequestEncoder,
      xmppEventFilter,
      xmppFilterOut
    );
  }

  public XmppConnectionHandler getConnectionHandler()
  {
    return mXmppConnectionHandler;
  }

  public void setChannel(Channel channel)
  {
    mXmppConnectionHandler = new XmppConnectionHandler(
      mNettyService,
      channel,
      mEventManager,
      mSslContext,
      mSsl,
      mChatProperties,
      mProxyAuthRequestEncoder,
      mXmppEventFilter,
      mXmppFilterOut
    );
  }

  public void resetSession(SessionManager sessionManager)
  {
    sessionManager.terminateSessionById(mXmppConnectionHandler.getSession().getId());
    mXmppConnectionHandler.setSession(
      new AnonymousXmppSession(
        SessionUUID.randomUUID(),
        new EventQueue(),
        mSocketChannel,
        mChatProperties,
        mXmppEventFilter,
        mXmppFilterOut
      ),
      sessionManager
    );
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg)
  {
    String stanza = (String) msg;
    ChatLog.log.debug("StanzaReader: " + stanza.trim().replaceAll("\n", "\\n"));

    ByteArrayInputStream xmlInputStream;
    try
    {
      xmlInputStream = new ByteArrayInputStream(stanza.getBytes("UTF-8"));
    }
    catch (UnsupportedEncodingException e)
    {
      throw new RuntimeException("unsupported utf-8?");
    }

    try
    {
      StanzaHandler handler = mXmppHandlerFactory.createHandler(mXmppConnectionHandler, stanza);
      handler.parse(xmlInputStream, mSchemaProvider);

      List<ChatOperation> operations = handler.handle();
      //ChatLog.log.debug("operation list: " + ZEUtils.objectToString(operations));
      mEventManager.execOperations(operations, mXmppConnectionHandler.getSession().getOutFilter());
    }
    catch (XmppHandlerFactory.UnkownStanza ex)
    {
      ChatLog.log.debug("Unknown stanza (XMPP): " + ex.getStanza());
    }
    catch (Throwable ex)
    {
      ChatLog.log.warn("Exception (XMPP): " + stanza + " " + Utils.exceptionToString(ex));
      mXmppConnectionHandler.close();
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
  {
    ChatLog.log.debug("exceptionCaught: " + Utils.exceptionToString(cause));
    ctx.close();
  }
}
