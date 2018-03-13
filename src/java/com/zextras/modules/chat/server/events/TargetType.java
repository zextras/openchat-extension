package com.zextras.modules.chat.server.events;

public enum TargetType
{
  Chat, GroupChat, Space, Channel;

  public static TargetType fromString(String typeString)
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

  public static TargetType fromShort(short typeString)
  {
    switch (typeString)
    {
      case 1: return GroupChat;
      case 2: return Space;
      case 3: return Channel;
      default: return Chat;
    }
  }

  public static short toShort(TargetType typeString)
  {
    switch (typeString)
    {
      case GroupChat: return 1;
      case Space: return 2;
      case Channel: return 3;
      default: return 0;
    }
  }
}