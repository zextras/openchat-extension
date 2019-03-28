package com.zextras.modules.chat.server.dispatch;

import com.google.inject.Singleton;
import com.zextras.lib.Optional;
import com.zextras.modules.chat.server.address.SpecificAddress;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Set;
import org.openzal.zal.lib.Version;

/**
 * This class provide a set of mailbox which hosts a room service
 *
 */

@Singleton
public class StubRoomServerHostSetProvider implements ServerHostSetProvider
{
  @Inject
  public StubRoomServerHostSetProvider(
  )
  {
  }

  @Override
  public Set<String> getRoomServers()
  {
    return Collections.emptySet();
  }

  @Override
  public Set<String> getAllServers()
  {
    return Collections.emptySet();
  }

  @Override
  public Set<SpecificAddress> getRoomServersAddresses()
  {
    return Collections.emptySet();
  }

  @Override
  public Set<SpecificAddress> getAllServersAddresses()
  {
    return Collections.emptySet();
  }

  public boolean isValidChatServer(SpecificAddress address)
  {
    return false;
  }

  public SpecificAddress selectNextServer(Optional<Version> minZextrasVersion, Optional<Version> minChatVersion)
  {
    return new SpecificAddress("localhost");
  }
}
