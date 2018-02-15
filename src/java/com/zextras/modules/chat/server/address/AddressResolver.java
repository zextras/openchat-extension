package com.zextras.modules.chat.server.address;

import com.zextras.modules.chat.server.exceptions.UnavailableResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AddressResolver
{
  /**
   * @param address to resolve
   * @return the server address
   * @throws UnavailableResource when the address cannot be resolved
   */
  @NotNull
  String resolveAddress(SpecificAddress address) throws UnavailableResource;

  /**
   * @param address to resolve
   * @return the server address or null if non is found
   */
  @Nullable
  String tryResolveAddress(SpecificAddress address);
}
