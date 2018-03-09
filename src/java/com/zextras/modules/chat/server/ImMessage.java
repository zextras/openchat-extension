package com.zextras.modules.chat.server;


import com.zextras.lib.Container;
import com.zextras.lib.ContainerImpl;
import com.zextras.lib.json.JSONException;
import com.zextras.lib.json.JSONObject;
import com.zextras.modules.chat.server.events.EventType;
import com.zextras.modules.chat.server.events.TargetType;

public class ImMessage
{
  private final String mId;
  private final long mEditTimestamp;
  private final long mSentTimestamp;
  private final TargetType mMessageType;
  private final EventType mEventType;
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
    TargetType type,
    long sentTimestamp
  )
  {
    this(
      id,
      sentTimestamp,
      0,
      EventType.Message,
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
    EventType eventType,
    TargetType messageType,
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
    mEventType = eventType;
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

  public TargetType getTargetType()
  {
    return mMessageType;
  }

  public EventType getEventType()
  {
    return mEventType;
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