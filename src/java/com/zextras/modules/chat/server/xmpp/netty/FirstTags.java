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
import com.zextras.modules.chat.server.SSLCipher;
import com.zextras.modules.chat.server.xmpp.XmppEventFilter;
import com.zextras.modules.chat.server.xmpp.XmppFilterOut;
import com.zextras.modules.core.services.NettyService;
import com.zextras.modules.chat.server.events.EventManager;
import com.zextras.modules.chat.server.xmpp.XmppHandlerFactory;
import com.zextras.modules.chat.server.xmpp.encoders.ProxyAuthRequestEncoder;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.openzal.zal.Utils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;

import javax.net.ssl.SSLContext;
import java.nio.charset.Charset;

public class FirstTags extends ChannelInboundHandlerAdapter
{
  private final XmppHandlerFactory                   mXmppHandlerFactory;
  private final EventManager                         mEventManager;
  private final SocketChannel                        mSocketChannel;
  private final SchemaProvider                       mSchemaProvider;
  private final boolean                              mSsl;
  private final ChatProperties                       mChatProperties;
  private final NettyService                         mNettyService;
  private final ProxyAuthRequestEncoder              mProxyAuthRequestEncoder;
  private final XmppEventFilter mXmppEventFilter;
  private final XmppFilterOut mXmppFilterOut;
  private final SSLContext mSslContext;
  private final SSLCipher mSslCipher;

  public FirstTags(
    XmppHandlerFactory xmppHandlerFactory,
    EventManager eventManager,
    SocketChannel socketChannel,
    SchemaProvider schemaProvider,
    boolean ssl,
    ChatProperties chatProperties,
    NettyService nettyService,
    ProxyAuthRequestEncoder proxyAuthRequestEncoder,
    XmppEventFilter xmppEventFilter,
    XmppFilterOut xmppFilterOut,
    SSLContext sslContext,
    SSLCipher sslCipher
  )
  {
    mXmppHandlerFactory = xmppHandlerFactory;
    mEventManager = eventManager;
    mSocketChannel = socketChannel;
    mSchemaProvider = schemaProvider;
    mSsl = ssl;
    mChatProperties = chatProperties;
    mNettyService = nettyService;
    mProxyAuthRequestEncoder = proxyAuthRequestEncoder;
    mXmppEventFilter = xmppEventFilter;
    mXmppFilterOut = xmppFilterOut;
    mSslContext = sslContext;
    mSslCipher = sslCipher;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg)
  {
    String xmlTag = (String) msg;
    ChatLog.log.debug("FirstTags: " + xmlTag);

    if (xmlTag.startsWith("<?xml"))
    {
      //TODO: parse new charset
      Charset newCharset = Charset.forName("UTF-8");

      ctx.pipeline().replace("SubTagTokenizer", "SubTagTokenizer", new XmlSubTagTokenizer(newCharset) );
    }
    else
    {
      if (xmlTag.trim().startsWith("<stream:stream"))
      {
        ctx.pipeline().addLast(
          "XmlTagTokenizer",
          new XmlTagTokenizer()
        );

        ctx.pipeline().addLast(
          "StanzaProcessor",
          new StanzaProcessor(
            mXmppHandlerFactory,
            mEventManager,
            mSocketChannel,
            mSchemaProvider,
            mSsl,
            mChatProperties,
            mNettyService,
            mProxyAuthRequestEncoder,
            mXmppEventFilter,
            mXmppFilterOut,
            mSslContext,
            mSslCipher
          )
        );
        ctx.fireChannelRead(xmlTag);
        // ctx.fireChannelRead("</stream:stream>");
        ctx.pipeline().remove("FirstTags");
      }
      else
      {
        throw new RuntimeException("Invalid first xml tag: " + xmlTag);
      }
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
  { // (4)
    ChatLog.log.warn("exceptionCaught: " + Utils.exceptionToString(cause));
    ctx.close();
  }
}
