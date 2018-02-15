package com.zextras.modules.chat.server.address;

import com.zextras.modules.chat.server.exceptions.UnavailableResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AddressResolverStub implements AddressResolver
{
  @NotNull
  @Override
  public String resolveAddress(SpecificAddress address) throws UnavailableResource
  {
    throw new UnavailableResource(address.toString());
  }

  @Nullable
  @Override
  public String tryResolveAddress(SpecificAddress address)
  {
    return null;
  }
}
