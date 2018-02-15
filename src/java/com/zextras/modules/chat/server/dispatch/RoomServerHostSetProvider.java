package com.zextras.modules.chat.server.dispatch;

import com.google.inject.Singleton;
import com.zextras.modules.chat.server.address.SpecificAddress;
import org.openzal.zal.Provisioning;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Set;

/**
 * This class provide a set of mailbox which hosts a room service
 *
 * TODO: working stub on monoserver, implement config + round-robin + multiserver
 */

@Singleton
public class RoomServerHostSetProvider
{
  private final Provisioning mProvisioning;

  @Inject
  public RoomServerHostSetProvider(
    Provisioning provisioning
  )
  {
    mProvisioning = provisioning;
  }

  public Set<String> get()
  {
    return Collections.singleton(
      mProvisioning.getLocalServer().getServerHostname()
    );
  }

  public Set<SpecificAddress> getAddresses()
  {
    return Collections.singleton(
      new SpecificAddress(mProvisioning.getLocalServer().getServerHostname())
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
