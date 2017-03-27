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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

import java.io.UnsupportedEncodingException;

public class OversizeStanzaManager
{
  public static final int    STANZA_SIZE_LIMIT = 64 * 1024;
  public static final String SIZE_LIMIT_VIOLATION =
    "<stream:error><policy-violation xmlns='urn:ietf:params:xml:ns:xmpp-streams'/><stanza-too-big xmlns='urn:xmpp:errors'/></stream:error></stream:stream>";

  public void manageOversize(ChannelHandlerContext ctx)
  {
    ByteBuf errorMessage;
    try
    {
      errorMessage = Unpooled.wrappedBuffer(SIZE_LIMIT_VIOLATION.getBytes("UTF-8"));
    }
    catch (UnsupportedEncodingException e)
    {
      throw new RuntimeException(e);
    }
    ChannelFuture future = ctx.writeAndFlush(errorMessage);
    future.awaitUninterruptibly(1000L);
    ctx.channel().close();
  }
}
