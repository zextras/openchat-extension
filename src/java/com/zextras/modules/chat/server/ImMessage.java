package com.zextras.modules.chat.server;


import com.zextras.modules.chat.server.events.EventType;

public class ImMessage
{
  private final String mId;
  private final long mEditTimestamp;
  private final long mSentTimestamp;
  private final EventType mMessageType;
  private final short mIndexStatus;
  private final String mSender;
  private final String mDestination;
  private final String mText;
  private final String mReactions;
  private final String mTypeExtrainfo;

  public ImMessage(
    String id,
    String sender,
    String destination,
    String text,
    EventType type,
    long sentTimestamp
  )
  {
    this(
      id,
      sentTimestamp,
      0,
      type,
      (short) 0,
      sender,
      destination,
      text,
      "",
      "");
  }

  public ImMessage(
    String id,
    long sentTimestamp,
    long editTimestamp,
    EventType messageType,
    short indexStatus,
    String sender,
    String destination,
    String text,
    String reactions,
    String typeExtrainfo)
  {
    mId = id;
    mSentTimestamp = sentTimestamp;
    mEditTimestamp = editTimestamp;
    mMessageType = messageType;
    mIndexStatus = indexStatus;
    mText = text;
    mSender = sender;
    mDestination = destination;
    mReactions = reactions;
    mTypeExtrainfo = typeExtrainfo;
  }

  public String getId()
  {
    return mId;
  }

  public long getSentTimestamp()
  {
    return mSentTimestamp;
  }

  public long getEditTimestamp()
  {
    return mEditTimestamp;
  }

  public EventType getMessageType()
  {
    return mMessageType;
  }

  public short getIndexStatus()
  {
    return mIndexStatus;
  }

  public String getText()
  {
    return mText;
  }

  public String getSender()
  {
    return mSender;
  }

  public String getDestination()
  {
    return mDestination;
  }

  public String getReactions()
  {
    return mReactions;
  }

  public String getTypeExtrainfo()
  {
    return mTypeExtrainfo;
  }
}