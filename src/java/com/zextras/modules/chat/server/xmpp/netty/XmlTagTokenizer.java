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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class XmlTagTokenizer extends MessageToMessageDecoder<String>
{
  private final OversizeStanzaManager mOversizeStanzaManager;
  private StringBuffer mStringBuffer = new StringBuffer(32 * 1024);
  int mDepth = 0;

  public XmlTagTokenizer()
  {
    this(new OversizeStanzaManager());
  }

  public XmlTagTokenizer(
    OversizeStanzaManager oversizeStanzaManager
  )
  {
    super();
    mOversizeStanzaManager = oversizeStanzaManager;
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out)
    throws Exception
  {
    //ZELog.chat.debug("XmlTagTokenizer: " + msg);

    if (msg.startsWith("<?"))
    {
      return;
    }

    mStringBuffer.append(msg);

    if (mStringBuffer.length() >= OversizeStanzaManager.STANZA_SIZE_LIMIT)
    {
      mOversizeStanzaManager.manageOversize(ctx);
      return;
    }

    if (StringUtils.deleteWhitespace(msg).startsWith("<stream:stream")) {
      out.add(mStringBuffer.toString());
      mStringBuffer.delete(0, mStringBuffer.length());
      return;
    }

    if( !msg.contains("/>") )
    {
      if( msg.contains("</") )
      {
        mDepth -= 1;
      }
      else
      {
        mDepth += 1;
      }
    }

    if( mDepth == 0 ) {
      out.add(mStringBuffer.toString());
      mStringBuffer.delete(0, mStringBuffer.length());
    }
  }
}
