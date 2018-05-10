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

package com.zextras.modules.chat.server.address;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.dispatch.RoomServerHostSetProvider;
import com.zextras.modules.chat.server.events.EventRouter;
import com.zextras.modules.chat.server.session.SessionUUID;
import com.zextras.modules.chat.server.dispatch.Dispatcher;
import com.zextras.modules.chat.server.dispatch.SpecificDispatcher;
import org.jetbrains.annotations.NotNull;

@JsonSerialize(using = ToStringSerializer.class)
public class SpecificAddress implements ChatAddress
{
  protected final String mAddress;
  protected final String mResource;

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
  public Dispatcher createDispatcher(EventRouter eventRouter, UserProvider openUserProvider, RoomServerHostSetProvider roomServerHostSetProvider)
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

  public boolean isFromSession( SessionUUID sessionUUID )
  {
    return false;
  }

  @Override
  public int hashCode()
  {
    return resourceAddress().hashCode();
  }

  @Override
  public String resource()
  {
    return mResource;
  }

  public SpecificAddress withoutSession()
  {
    return this;
  }

  public SpecificAddress withoutResource()
  {
    if( mResource.isEmpty() )
    {
      return this;
    }
    else
    {
      return new SpecificAddress(mAddress, "");
    }
  }

  @NotNull
  public String getDomain()
  {
    int idx = mAddress.indexOf('@');
    if( idx > 0 )
    {
      return mAddress.substring(idx+1);
    }
    else
    {
      return "";
    }
  }

  public SpecificAddress intern()
  {
    return new SpecificAddress(
      mAddress.intern(),
      mResource.intern()
    );
  }
}
