package com.zextras.modules.chat.server.events;

public enum EventType
{
  Message,
  SharedFile,
  ConversationCreated,
  ConversationMemberJoined,
  ConversationMemberLeft,
  ConversationNameChanged,
  ConversationTopicChanged,
  ConversationPictureChanged,
  MeetingStarted,
  MeetingEnded
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
      case 6: return ConversationTopicChanged;
      case 7: return ConversationPictureChanged;
      case 8: return MeetingStarted;
      case 9: return MeetingEnded;
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
      case ConversationTopicChanged: return 6;
      case ConversationPictureChanged: return 7;
      case MeetingStarted: return 8;
      case MeetingEnded: return 9;
      default: throw new RuntimeException("invalid: "+type);
    }
  }
}
