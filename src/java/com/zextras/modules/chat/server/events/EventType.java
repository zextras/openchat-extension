package com.zextras.modules.chat.server.events;

public enum EventType
{
  Message, SharedFile,
  ;

  public static EventType fromShort(short typeString)
  {
    switch (typeString)
    {
      case 1: return SharedFile;
      default: return Message;
    }
  }

  public static short toShort(EventType type)
  {
    switch (type)
    {
      case SharedFile: return 1;
      default: return 0;
    }
  }
}
