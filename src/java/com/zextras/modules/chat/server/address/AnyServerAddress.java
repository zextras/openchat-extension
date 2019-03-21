package com.zextras.modules.chat.server.address;

import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.dispatch.AnyServerDispatcher;
import com.zextras.modules.chat.server.dispatch.Dispatcher;
import com.zextras.modules.chat.server.dispatch.ServerHostSetProvider;
import com.zextras.modules.chat.server.events.EventRouter;
import com.zextras.modules.chat.server.session.SessionUUID;

public class AnyServerAddress implements ChatAddress
{
  public static final AnyServerAddress sInstance = new AnyServerAddress();

  @Override
  public Dispatcher createDispatcher(
    EventRouter eventRouter,
    UserProvider openUserProvider,
    ServerHostSetProvider roomServerHostSetProvider
  )
  {
    return new AnyServerDispatcher(
      eventRouter,
      roomServerHostSetProvider
    );
  }

  @Override
  public String resource()
  {
    return "";
  }

  @Override
  public String resourceAddress()
  {
    return "";
  }

  @Override
  public ChatAddress withoutSession()
  {
    return this;
  }

  @Override
  public ChatAddress withoutResource()
  {
    return this;
  }

  @Override
  public boolean isFromSession(SessionUUID sessionUUID)
  {
    return false;
  }
}
