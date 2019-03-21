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

import com.zextras.modules.chat.server.address.ChatAddress;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.dispatch.ServerHostSetProvider;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventRouter;
import com.zextras.modules.chat.server.exceptions.ChatException;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class Target
{
  private final Collection<ChatAddress> mAddresses;
  private HashSet<SpecificAddress> mExplodedSet = new HashSet<SpecificAddress>();
  private boolean                  mExploded    = false;

  public Target(ChatAddress... addresses)
  {
    mAddresses = Arrays.asList(addresses);
  }

  public Target(Collection<ChatAddress> addresses)
  {
    mAddresses = addresses;
  }

  public void dispatch(EventRouter eventRouter, UserProvider openUserProvider, ServerHostSetProvider roomServerHostSetProvider, Event event)
    throws ChatException
  {
    for (ChatAddress address : mAddresses)
    {
      address.createDispatcher(eventRouter, openUserProvider, roomServerHostSetProvider).dispatch(event);
    }
  }


  @Override
  public boolean equals(Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

    Target target = (Target) o;

    if (!mAddresses.equals(target.mAddresses))
    {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    return mAddresses.hashCode();
  }

  @Override
  public String toString()
  {
    String addresses = "";
    for(ChatAddress address : mAddresses)
    {
      if(!addresses.contains(address.resourceAddress()))
      {
        if(!addresses.isEmpty())
        {
          addresses = addresses + ", ";
        }
        addresses = addresses + address.resourceAddress();
      }
    }

    return addresses;
  }

  public String toSingleAddress()
  {
    if( mAddresses.size() != 1 )
    {
      throw new RuntimeException();
    }

    // TODO toString bad bad
    return mAddresses.iterator().next().toString();
  }

  public String toSingleAddressIncludeResource()
  {
    if( mAddresses.size() != 1 )
    {
      throw new RuntimeException();
    }

    return mAddresses.iterator().next().resourceAddress();
  }

  public Collection<ChatAddress> getAddresses()
  {
    return mAddresses;
  }
}
