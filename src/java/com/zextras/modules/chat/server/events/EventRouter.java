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

package com.zextras.modules.chat.server.events;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zextras.lib.log.ChatLog;
import com.zextras.lib.switches.Service;
import com.zextras.modules.chat.server.address.SpecificAddress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Singleton
public class EventRouter implements Service
{
  protected List<EventDestinationProvider> mEventDestinationProviderList;

  @Inject
  public EventRouter()
  {
    mEventDestinationProviderList = new ArrayList<EventDestinationProvider>(16);
  }

  public void plugDestinationProvider( EventDestinationProvider provider )
  {
    if (mEventDestinationProviderList.contains( provider ))
    {
      throw new RuntimeException("Adding EventDestinationProvider multiple times");
    }
    mEventDestinationProviderList.add( provider );
    Collections.sort(mEventDestinationProviderList, new EventDestinationSorter());
  }

  public void unplugDestinationProvider( EventDestinationProvider provider )
  {
    mEventDestinationProviderList.remove( provider );
  }


  public void deliverEvent(SpecificAddress address, Event event)
  {
    for( EventDestinationProvider provider : mEventDestinationProviderList )
    {
      for( EventDestination destination : provider.getDestinations(address) )
      {
        boolean eventAccepted = destination.deliverEvent(event,address);
        if( eventAccepted )
        {
          //ChatLog.log.debug("Routing: processed " + event.toString() + " => "  + destination.getClass().getSimpleName());
        }
        //ChatLog.log.info("Routing: " + event.getId() + (eventAccepted ? " ACCEPTED" : " REFUSED") + " by " + destination.getClass().getSimpleName());
      }
    }
  }

  @Override
  public void start() throws ServiceStartException
  {}

  @Override
  public void stop()
  {
    mEventDestinationProviderList.clear();
  }

  private class EventDestinationSorter implements Comparator<EventDestinationProvider>
  {
    @Override
    public int compare(EventDestinationProvider o1, EventDestinationProvider o2) {
      return o1.getPriority().getValue().compareTo(o2.getPriority().getValue());
    }
  }
}
