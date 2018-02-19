package com.zextras.modules.chat.server.dispatch;

import com.google.inject.Singleton;
import com.zextras.modules.chat.server.address.SpecificAddress;
import org.openzal.zal.Provisioning;
import org.openzal.zal.Server;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
    List<Server> servers = mProvisioning.getAllServers();
    Set<String> hosts = new HashSet<String>(servers.size());
    for (Server server : servers)
    {
      hosts.add(server.getServerHostname());
    }
    return hosts;
  }

  public Set<SpecificAddress> getAddresses()
  {
    Set<String> servers = get();
    Set<SpecificAddress> addresses = new HashSet<SpecificAddress>(servers.size());
    for (String s : servers)
    {
      addresses.add(new SpecificAddress(s));
    }
    return addresses;
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
