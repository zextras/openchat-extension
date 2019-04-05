package com.zextras.modules.chat.server.dispatch;

import com.zextras.lib.Error.ZxError;
import com.zextras.lib.Optional;
import com.zextras.modules.chat.server.address.SpecificAddress;

import java.util.Set;
import org.openzal.zal.lib.Version;

public interface ServerHostSetProvider
{
  Set<String> getRoomServers();
  Set<String> getAllServers();
  Set<SpecificAddress> getRoomServersAddresses();
  Set<SpecificAddress> getAllServersAddresses();
  boolean isValidChatServer(SpecificAddress address);
  SpecificAddress selectNextServer(Optional<Version> minZextrasVersion, Optional<Version> minChatVersion) throws ZxError;
}
