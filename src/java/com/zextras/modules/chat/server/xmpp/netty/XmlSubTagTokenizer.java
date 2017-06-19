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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.Charset;
import java.util.List;

public class XmlSubTagTokenizer extends ByteToMessageDecoder
{
  static final byte sTokenEnd = '>';
  private final Charset mCharset;

  public XmlSubTagTokenizer()
  {
    mCharset = Charset.forName("UTF-8");
  }

  XmlSubTagTokenizer(Charset charset)
  {
    mCharset = charset;
  }

  @Override
  protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> objects) throws Exception
  {
    int idx = byteBuf.bytesBefore(sTokenEnd);
    if (idx == -1) {
      return;
    }

    String token = byteBuf.toString(0, idx + 1, mCharset);
    byteBuf.readerIndex(byteBuf.readerIndex() + idx + 1);
    byteBuf.discardReadBytes();
    objects.add(token);
  }
}
