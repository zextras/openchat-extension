package com.zextras.modules.chat.server.events;

import com.zextras.modules.chat.server.exceptions.ChatException;

public class EventInterpreterAdapter<T> implements EventInterpreter<T>
{
  protected final T mDefaultValue;

  public EventInterpreterAdapter(T defaultValue)
  {
    mDefaultValue = defaultValue;
  }

  @Override
  public T interpret(Event event) throws ChatException
  {
    return mDefaultValue;
  }
  
  @Override
  public T interpret(EventStatusProbe eventStatusProbe) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventStatuses eventStatuses) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventStatusChanged eventStatusChanged) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventSoapSessionRegistered eventSoapSessionRegistered) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventNewClientVersion eventNewClientVersion) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventMessageSizeExceeded eventMessageSizeExceeded) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventMessageBack eventMessageBack) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventMessageAck eventMessageAck) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventMessage eventMessage) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventIsWriting eventIsWriting) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventGetRelationships eventGetRelationships) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventGetPrivacy eventGetPrivacy) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventFriendRenamed eventFriendRenamed) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventFriendBackRemove eventFriendBackRemove) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventFriendBackAdded eventFriendBackAdded) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventFriendBackAccepted eventFriendBackAccepted) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventFriendAddedForClient eventFriendAddedForClient) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventFriendAdded eventFriendAdded) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventFriendAccepted eventFriendAccepted) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventBindResult eventBindResult) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(FriendNotFoundEvent friendNotFoundEvent) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(FeatureNotImplementedEvent featureNotImplementedEvent) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventStreamStarted eventXmppSessionRegistered) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventIQAuthResult eventIqAuthResult) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventXmppSASLAuthentication eventXmppSASLAuthentication) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventXmppSessionFeatures eventXmppSessionFeatures) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventXmppRedirect eventXmppRedirect) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventXmppSessionEstablished eventXmppSessionEstablished) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventXmppPing eventXmppPing) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(EventXmppDiscovery event) throws ChatException
  {
    return mDefaultValue;
  }

  @Override
  public T interpret(final EventFloodControl event) throws ChatException
  {
    return mDefaultValue;
  }
}
