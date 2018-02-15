package com.zextras.modules.chat.server.events;

public enum EventType
{
  Chat, GroupChat, Space, Channel;

  public static EventType fromString(String typeString)
  {
    if (GroupChat.name().equalsIgnoreCase(typeString))
    {
      return GroupChat;
    }

    if (Space.name().equalsIgnoreCase(typeString))
    {
      return Space;
    }

    if (Channel.name().equalsIgnoreCase(typeString))
    {
      return Channel;
    }

    return Chat;
  }
}
