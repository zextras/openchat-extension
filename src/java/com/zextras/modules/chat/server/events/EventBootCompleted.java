package com.zextras.modules.chat.server.events;

import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.NoneAddress;
import com.zextras.modules.chat.server.exceptions.ChatException;

/**
 * This event is meant to be directly injected to the EventRouter
 *
 * @see EventRouter
 */
public class EventBootCompleted extends Event
{
  public EventBootCompleted()
  {
    super(NoneAddress.sInstance, new Target());
  }

  @Override
  public <T> T interpret(EventInterpreter<T> interpreter) throws ChatException
  {
    return interpreter.interpret(this);
  }
}
