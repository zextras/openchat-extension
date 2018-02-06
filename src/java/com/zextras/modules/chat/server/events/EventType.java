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

    return Chat;
  }
}
