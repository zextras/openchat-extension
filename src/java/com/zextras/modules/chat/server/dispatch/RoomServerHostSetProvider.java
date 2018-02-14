package com.zextras.modules.chat.server.dispatch;

import com.zextras.modules.chat.server.address.SpecificAddress;

import java.util.Collections;
import java.util.Set;

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

  public boolean isValidServer(SpecificAddress address)
  {
    return getAddresses().contains(address);
  }
}
