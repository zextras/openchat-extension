package com.zextras.modules.chat.server.dispatch;

import com.zextras.modules.chat.server.address.SpecificAddress;

import java.util.Collections;
import java.util.Set;

/**
 * This class provide a set of mailbox which hosts a room service
 *
 * TODO: working stub, implement me
 */
public class RoomServerHostSetProvider
{
  public Set<String> get()
  {
    return Collections.singleton("localhost");
  }

  public Set<SpecificAddress> getAddresses()
  {
    return Collections.singleton(
      new SpecificAddress("localhost")
    );
  }

  public boolean isValidChatServer(SpecificAddress address)
  {
    return getAddresses().contains(address);
  }

  public SpecificAddress selectNextServer()
  {
    return getAddresses().iterator().next();
  }
}
