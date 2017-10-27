package com.zextras.modules.chat.server.events;

public class EventInterpreterAdapter<T> implements EventInterpreter<T>
{
  protected final T mDefaultValue;

  public EventInterpreterAdapter(T defaultValue)
  {
    mDefaultValue = defaultValue;
  }

  @Override
  public T interpret(Event event)
  {
    return mDefaultValue;
  }
  
  @Override
  public T interpret(EventStatusProbe eventStatusProbe)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventStatuses eventStatuses)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventStatusChanged eventStatusChanged)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventSoapSessionRegistered eventSoapSessionRegistered)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventNewClientVersion eventNewClientVersion)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventMessageSizeExceeded eventMessageSizeExceeded)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventMessageBack eventMessageBack)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventMessageAck eventMessageAck)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventMessage eventMessage)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventIsWriting eventIsWriting)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventGetRelationships eventGetRelationships)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventGetPrivacy eventGetPrivacy)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventFriendRenamed eventFriendRenamed)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventFriendBackRemove eventFriendBackRemove)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventFriendBackAdded eventFriendBackAdded)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventFriendBackAccepted eventFriendBackAccepted)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventFriendAddedForClient eventFriendAddedForClient)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventFriendAdded eventFriendAdded)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventFriendAccepted eventFriendAccepted)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventBindResult eventBindResult)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(FriendNotFoundEvent friendNotFoundEvent)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(FeatureNotImplementedEvent featureNotImplementedEvent)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventStreamStarted eventXmppSessionRegistered)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventIQAuthResult eventIqAuthResult)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventXmppSASLAuthentication eventXmppSASLAuthentication)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventXmppSessionFeatures eventXmppSessionFeatures)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventXmppRedirect eventXmppRedirect)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventXmppSessionEstablished eventXmppSessionEstablished)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventXmppPing eventXmppPing)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventXmppDiscovery event)
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(final EventFloodControl event)
  {
    return mDefaultValue;
  }
}
