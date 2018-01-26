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

import com.zextras.modules.chat.server.address.NoneAddress;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class EventXmppDiscovery extends Event
{
  private final Collection<String> mFeatures;
  private final List<Result>       mResults;
  private final DiscoveryQuery     mDiscoveryQuery;
  private final String             mType;
  private final SpecificAddress mSender;

  public EventXmppDiscovery(
    EventId eventId,
    SpecificAddress sender,
    Target target,
    String type,
    Collection<String> features,
    List<EventXmppDiscovery.Result> results,
    EventXmppDiscovery.DiscoveryQuery discoveryQuery
  )
  {
    super(eventId, sender, target);
    mFeatures = features;
    mResults = results;
    mDiscoveryQuery = discoveryQuery;
    mType = type;
    mSender = sender;
  }

  public EventXmppDiscovery(
    EventId eventId,
    SpecificAddress sender,
    Target target,
    EventXmppDiscovery.DiscoveryQuery discoveryQuery
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
      ", mId=" + getId() +
      ", mSender=" + mSender.resourceAddress() +
      ", mTimestamp=" + getTimestamp() +
      ", mTarget=" + getTarget().toString() +
      ", mFeatures=" + mFeatures +
      ", mResults=" + mResults +
      ", mDiscoveryQuery=" + mDiscoveryQuery +
      ", mType='" + mType + '\'' +
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
      return "{" +mAddress+ ',' +mName+'}';
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

    public static EventXmppDiscovery.DiscoveryQuery fromUrl(String url)
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
      for (EventXmppDiscovery.DiscoveryQuery type : DiscoveryQuery.values())
      {
        if (type.getUrl().equalsIgnoreCase(url))
        {
          return true;
        }
      }

      return false;
    }
  }
}
