package com.zextras.modules.chat.server.events;

public enum EventType
{
  Message, SharedFile, ConversationCreated,
  ;

  public static EventType fromShort(short typeString)
  {
    switch (typeString)
    {
      case 0: return Message;
      case 1: return SharedFile;
      case 2: return ConversationCreated;
      default: throw new RuntimeException("invalid: "+typeString);
    }
  }

  public static short toShort(EventType type)
  {
    switch (type)
    {
      case Message: return 0;
      case SharedFile: return 1;
      case ConversationCreated: return 2;
      default: throw new RuntimeException("invalid: "+type);
    }
  }
}
