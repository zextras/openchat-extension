package com.zextras.modules.chat.server.address;

import com.zextras.modules.chat.server.exceptions.UnavailableResource;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AddressResolverStub implements AddressResolver
{
  @Nonnull
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
