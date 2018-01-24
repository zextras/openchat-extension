package com.zextras.modules.chat.server;


public class ImMessage
{
  private final String mId;
  private final long mEditTimestamp;
  private final long mSentTimestamp;
  private final short mMessageType;
  private final boolean mIsMultichat;
  private final short mIndexStatus;
  private final String mSender;
  private final String mDestination;
  private final String mText;
  private final String mReactions;
  private final String mTypeExtrainfo;
  private String mDelivered;

  public ImMessage(
    String id,
    String sender,
    String destination,
    String text,
    long sentTimestamp
  )
  {
    this(
      id,
      sentTimestamp,
      (long) 0,
      (short) 0,
      false,
      (short) 0,
      sender,
      destination,
      text,
      "",
      "",
      "");
  }

  public ImMessage(
    String id,
    String sender,
    String destination,
    String text)
  {
    this(
      id,
      System.currentTimeMillis(),
      (long) 0,
      (short) 0,
      false,
      (short) 0,
      sender,
      destination,
      text,
      "",
      "",
      "");
  }

  public ImMessage(
    String id,
    long sentTimestamp,
    long editTimestamp,
    short messageType,
    boolean isMultichat,
    short indexStatus,
    String sender,
    String destination,
    String text,
    String reactions,
    String typeExtrainfo,
    String delivered)
  {
    mId = id;
    mSentTimestamp = sentTimestamp;
    mEditTimestamp = editTimestamp;
    mMessageType = messageType;
    mIsMultichat = isMultichat;
    mIndexStatus = indexStatus;
    mText = text;
    mSender = sender;
    mDestination = destination;
    mReactions = reactions;
    mTypeExtrainfo = typeExtrainfo;
    mDelivered = delivered;
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

  public short getMessageType()
  {
    return mMessageType;
  }

  public boolean isMultichat()
  {
    return mIsMultichat;
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

  public String getDelivered()
  {
    return mDelivered;
  }

  public void setDelivered(String delivered)
  {
    mDelivered = delivered;
  }
}