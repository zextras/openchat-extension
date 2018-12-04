package com.zextras.modules.chat.server.address;

import com.zextras.modules.chat.server.events.TargetType;

/**
 * Conversion from and to chat subdomain.
 *
 * TODO:
 *  - introduce configuration option to change the subdomain
 */
public class SubdomainResolver
{
  public String getSubdomainFor(String domain)
  {
    return "chat."+domain;
  }

  public String removeSubdomainFrom(TargetType eventType, String room)
  {
    return room;
  }

  public String removeSubdomainFrom(String room)
  {
    return room;
  }

  public SpecificAddress toRoomAddress(TargetType eventType, String room)
  {
    return new SpecificAddress(room);
  }

  public SpecificAddress toRoomAddress(String room)
  {
    return new SpecificAddress(room);
  }
}
