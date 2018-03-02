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

import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.exceptions.ChatException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class EventDiscovery extends Event
{
  private final Collection<String> mFeatures;
  private final List<Result>       mResults;
  private final DiscoveryQuery     mDiscoveryQuery;
  private final String             mType;
  private final SpecificAddress    mSender;

  public EventDiscovery(
    EventId eventId,
    SpecificAddress sender,
    Target target,
    String type,
    Collection<String> features,
    List<EventDiscovery.Result> results,
    EventDiscovery.DiscoveryQuery discoveryQuery
  )
  {
    super(eventId, sender, target);
    mFeatures = features;
    mResults = results;
    mDiscoveryQuery = discoveryQuery;
    mType = type;
    mSender = sender;
  }

  public EventDiscovery(
    EventId eventId,
    SpecificAddress sender,
    Target target,
    EventDiscovery.DiscoveryQuery discoveryQuery
  )
  {
    super(eventId, sender, target);
    mFeatures = Collections.emptyList();
    mResults = Collections.emptyList();
    mDiscoveryQuery = discoveryQuery;
    mType = "get";
    mSender = sender;
  }

  public DiscoveryQuery getDiscoveryQuery()
  {
    return mDiscoveryQuery;
  }

  public String getType()
  {
    return mType;
  }

  public boolean isResult()
  {
    return "result".equalsIgnoreCase(mType);
  }

  public List<Result> getResults()
  {
    return mResults;
  }

  public Collection<String> getFeatures()
  {
    return mFeatures;
  }

  @Override
  public SpecificAddress getSender()
  {
    return mSender;
  }

  @Override
  public <T> T interpret(EventInterpreter<T> interpreter) throws ChatException
  {
    return interpreter.interpret(this);
  }

  @Override
  public String toString()
  {
    return "Event{" +
      getClass().getSimpleName() +
      ", id=" + getId() +
      ", sender=" + mSender.resourceAddress() +
      ", timestamp=" + getTimestamp() +
      ", target=" + getTarget().toString() +
      ", features=" + mFeatures +
      ", results=" + mResults +
      ", discoveryQuery=" + mDiscoveryQuery +
      ", type='" + mType + '\'' +
      '}';
  }

  public static class Result
  {
    public SpecificAddress getRoomAddress()
    {
      return mAddress;
    }

    public String getName()
    {
      return mName;
    }

    private final SpecificAddress mAddress;
    private final String          mName;

    public Result(SpecificAddress address, String name)
    {
      mAddress = address;
      mName = name;
    }

    @Override
    public String toString()
    {
      return "{" + mAddress + ',' + mName + '}';
    }

    @Override
    public boolean equals(Object o)
    {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;

      Result result = (Result) o;

      if (!mAddress.equals(result.mAddress))
        return false;
      return mName.equals(result.mName);
    }

    @Override
    public int hashCode()
    {
      int result = mAddress.hashCode();
      result = 31 * result + mName.hashCode();
      return result;
    }
  }

  public enum DiscoveryQuery
  {
    info("http://jabber.org/protocol/disco#info"),
    items("http://jabber.org/protocol/disco#items");

    private final String mUrl;

    DiscoveryQuery(String url)
    {
      mUrl = url;
    }

    public static EventDiscovery.DiscoveryQuery fromUrl(String url)
    {
      for (DiscoveryQuery type : DiscoveryQuery.values())
      {
        if (type.getUrl().equalsIgnoreCase(url))
        {
          return type;
        }
      }

      throw new RuntimeException();
    }

    public String getUrl()
    {
      return mUrl;
    }

    public static boolean isSupported(String url)
    {
      for (EventDiscovery.DiscoveryQuery type : DiscoveryQuery.values())
      {
        if (type.getUrl().equalsIgnoreCase(url))
        {
          return true;
        }
      }

      return false;
    }
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    if (!super.equals(o))
      return false;

    EventDiscovery that = (EventDiscovery) o;

    if (!mFeatures.equals(that.mFeatures))
      return false;
    if (!mResults.equals(that.mResults))
      return false;
    if (mDiscoveryQuery != that.mDiscoveryQuery)
      return false;
    if (!mType.equals(that.mType))
      return false;
    return mSender.equals(that.mSender);
  }

  @Override
  public int hashCode()
  {
    int result = super.hashCode();
    result = 31 * result + mFeatures.hashCode();
    result = 31 * result + mResults.hashCode();
    result = 31 * result + mDiscoveryQuery.hashCode();
    result = 31 * result + mType.hashCode();
    result = 31 * result + mSender.hashCode();
    return result;
  }
}
