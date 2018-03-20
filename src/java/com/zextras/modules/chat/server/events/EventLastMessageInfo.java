package com.zextras.modules.chat.server.events;

import com.zextras.lib.Optional;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.exceptions.ChatException;
import org.apache.commons.lang3.tuple.Pair;
import org.openzal.zal.lib.ActualClock;
import org.openzal.zal.lib.Clock;

public class EventLastMessageInfo extends Event
{
  private static final long ONE_MONTH = 1000L * 60L * 60L * 24L * 30L;

  private final Optional<SpecificAddress>    mBuddyAddress;
  private final Optional<Pair<Long, String>> mLastSentMessageInfo;
  private final Optional<SpecificAddress>    mFinalDestination;
  private final Optional<Integer>            mUnreadCount;
  private final Optional<Pair<Long, String>> mLastIncomingMessageInfo;
  private final Clock                        mClock;

  public EventLastMessageInfo(
    SpecificAddress finalDestination,
    SpecificAddress destinationAddress,
    Optional<Pair<Long, String>> lastSentMessageInfo
  )
  {
    this(
      finalDestination,
      destinationAddress,
      Optional.sEmptyInstance,
      lastSentMessageInfo
    );
  }

  public EventLastMessageInfo(
    SpecificAddress finalDestination,
    SpecificAddress destinationAddress,
    Optional<SpecificAddress> buddyAddress,
    Optional<Pair<Long, String>> lastSentMessageInfo
  )
  {
    this(
      finalDestination,
      new Target(destinationAddress),
      buddyAddress,
      lastSentMessageInfo,
      Optional.sEmptyInstance,
      Optional.sEmptyInstance,
      Optional.of(finalDestination)
    );
  }

  public EventLastMessageInfo(
    SpecificAddress sender,
    Target target,
    Optional<SpecificAddress> buddyAddress,
    Optional<Pair<Long, String>> lastSentMessageInfo,
    Optional<Pair<Long, String>> lastIncomingMessageInfo,
    Optional<Integer> unreadCount,
    Optional<SpecificAddress> finalDestination
  )
  {
    this(
      EventId.randomUUID(),
      sender,
      target,
      System.currentTimeMillis(),
      buddyAddress,
      lastSentMessageInfo,
      lastIncomingMessageInfo,
      unreadCount,
      finalDestination
    );
  }

  public EventLastMessageInfo(
    EventId eventId,
    SpecificAddress sender,
    Target target,
    long timestamp,
    Optional<SpecificAddress> buddyAddress,
    Optional<Pair<Long, String>> lastSentMessageInfo,
    Optional<Pair<Long, String>> lastIncomingMessageInfo,
    Optional<Integer> unreadCount,
    Optional<SpecificAddress> finalDestination
  )
  {
    super(eventId, sender, target, timestamp);
    mBuddyAddress = buddyAddress;
    mLastSentMessageInfo = lastSentMessageInfo;
    mLastIncomingMessageInfo = lastIncomingMessageInfo;
    mUnreadCount = unreadCount;
    mFinalDestination = finalDestination;
    mClock = new ActualClock();
  }

  public Optional<Pair<Long, String>> getLastSentMessageInfo()
  {
    return mLastSentMessageInfo;
  }

  public Optional<Pair<Long, String>> getLastIncomingMessageInfo()
  {
    return mLastIncomingMessageInfo;
  }

  public Optional<Integer> getUnreadCount()
  {
    return mUnreadCount;
  }

  public Optional<SpecificAddress> getFinalDestination()
  {
    return mFinalDestination;
  }

  public Optional<SpecificAddress> getBuddyAddress()
  {
    return mBuddyAddress;
  }

  @Override
  public <T> T interpret(EventInterpreter<T> interpreter) throws ChatException
  {
    return interpreter.interpret(this);
  }

  public boolean isRequired()
  {
    return !mFinalDestination.hasValue() && (
        (mUnreadCount.hasValue() && mUnreadCount.getValue() > 0) ||
        (mLastIncomingMessageInfo.hasValue() && mLastIncomingMessageInfo.getValue().getLeft() > mClock.now() - ONE_MONTH) ||
        (mLastSentMessageInfo.hasValue() && mLastSentMessageInfo.getValue().getLeft() > mClock.now() - ONE_MONTH)
      );
  }
}
