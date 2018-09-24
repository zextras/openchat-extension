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

package com.zextras.modules.core.services;

import com.google.inject.Singleton;
import com.zextras.modules.core.netty.EventLoopGroupProvider;
import com.zextras.lib.switches.Service;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/*
  We keep only one thread pool for netty to avoid multiple thread pools
*/
@Singleton
public class NettyService implements Service, EventLoopGroupProvider
{
  private static final int NUMBER_OF_THREADS = 16;
  protected EventLoopGroup mEventLoopGroup;

  public NettyService()
  {
    mEventLoopGroup = null;
  }

  @Override
  public void start() throws ServiceStartException
  {
    mEventLoopGroup = new NioEventLoopGroup(NUMBER_OF_THREADS);
  }

  @Override
  public void stop()
  {
    mEventLoopGroup.shutdownGracefully().syncUninterruptibly();
    mEventLoopGroup = null;
  }

  @Override
  public EventLoopGroup getEventLoopGroup()
  {
    if( mEventLoopGroup == null ) {
      throw new RuntimeException();
    }
    return mEventLoopGroup;
  }
}
