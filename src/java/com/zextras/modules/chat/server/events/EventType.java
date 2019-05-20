package com.zextras.modules.chat.server.events;

public enum EventType
{
  Message,
  SharedFile,
  ConversationCreated,
  ConversationMemberJoined,
  ConversationMemberLeft,
  ConversationNameChanged
  ;

  public static EventType fromShort(short typeString)
  {
    switch (typeString)
    {
      case 0: return Message;
      case 1: return SharedFile;
      case 2: return ConversationCreated;
      case 3: return ConversationMemberJoined;
      case 4: return ConversationMemberLeft;
      case 5: return ConversationNameChanged;
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
      case ConversationMemberJoined: return 3;
      case ConversationMemberLeft: return 4;
      case ConversationNameChanged: return 5;
      default: throw new RuntimeException("invalid: "+type);
    }
  }
}
