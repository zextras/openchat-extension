package com.zextras.modules.chat.server.address;

import com.zextras.modules.chat.server.events.TargetType;

/**
 * Conversion from and to chat subdomain.
 *
 */
public class SubdomainResolver
{
  public String getSubdomainFor(String domain)
  {
    return "__room."+domain;
  }

  public String removeSubdomainFrom(String room)
  {
    return room.replace("@__room.", "@.");
  }

  public SpecificAddress toRoomAddress(TargetType eventType, String room)
  {
    if (eventType != TargetType.Chat)
    {
      String[] strings = room.split("@");
      if (strings.length >= 2)
      {
        return new SpecificAddress(strings[0] + "@__room" + strings[1]);
      }
    }
    return new SpecificAddress(room);
  }
}
