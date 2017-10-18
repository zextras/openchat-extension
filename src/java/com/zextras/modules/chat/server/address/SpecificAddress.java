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

package com.zextras.modules.chat.server.address;

import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.events.EventRouter;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.session.SessionUUID;
import com.zextras.modules.chat.server.dispatch.Dispatcher;
import com.zextras.modules.chat.server.dispatch.SpecificDispatcher;

import java.util.HashSet;

public class SpecificAddress implements ChatAddress
{
  private final String mAddress;
  private final String mResource;

  public SpecificAddress(String address)
  {
   int idx = address.indexOf('/');
   if( idx != -1 )
   {
     mAddress = address.substring(0,idx);
     mResource = address.substring(idx+1);
   }
   else
   {
     mAddress = address;
     mResource = "";
   }
  }

  public SpecificAddress(String address, String resource)
  {
    mAddress = address;
    mResource = resource;
  }

  @Override
  public Dispatcher createDispatcher(EventRouter eventRouter, UserProvider openUserProvider)
  {
    return new SpecificDispatcher(this, eventRouter);
  }

  public boolean equals(Object object)
  {
    if(!(object instanceof SpecificAddress))
    {
      return false;
    }

    SpecificAddress specific = (SpecificAddress) object;

    return mAddress.equals(specific.mAddress);
  }

  public boolean fullyEquals(Object object)
  {
    if(!(object instanceof SpecificAddress))
    {
      return false;
    }

    SpecificAddress specific = (SpecificAddress) object;

    return mAddress.equals(specific.mAddress) &&
           mResource.equals(specific.mResource);
  }

  @Override
  public String toString()
  {
    return mAddress;
  }

  @Override
  public String resourceAddress()
  {
    if( mResource.isEmpty() ) {
      return mAddress;
    }
    return mAddress+'/'+mResource;
  }

  @Override
  public void explode(HashSet<SpecificAddress> explodedSet, UserProvider openUserProvider) throws ChatDbException
  {
    explodedSet.add(this);
  }

  public boolean isFromSession( SessionUUID sessionUUID )
  {
    return false;
  }

  @Override
  public int hashCode()
  {
    return mAddress.hashCode();
  }

  public String resource()
  {
    return mResource;
  }

  public SpecificAddress withoutSession()
  {
    return this;
  }

  public SpecificAddress intern()
  {
    return new SpecificAddress(
      mAddress.intern(),
      mResource.intern()
    );
  }
}
