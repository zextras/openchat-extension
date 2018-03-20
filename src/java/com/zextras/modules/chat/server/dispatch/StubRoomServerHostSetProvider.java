package com.zextras.modules.chat.server.dispatch;

import com.google.inject.Singleton;
import com.zextras.modules.chat.server.address.SpecificAddress;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Set;

/**
 * This class provide a set of mailbox which hosts a room service
 *
 */

@Singleton
public class StubRoomServerHostSetProvider implements RoomServerHostSetProvider
{
  @Inject
  public StubRoomServerHostSetProvider(
  )
  {
  }

  public Set<String> get()
  {
    return Collections.EMPTY_SET;
  }

  public Set<SpecificAddress> getAddresses()
  {
    return Collections.EMPTY_SET;
  }

  public boolean isValidChatServer(SpecificAddress address)
  {
    return false;
  }

  public SpecificAddress selectNextServer()
  {
    return new SpecificAddress("localhost");
  }
}
