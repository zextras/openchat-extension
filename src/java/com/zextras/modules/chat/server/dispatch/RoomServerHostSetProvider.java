package com.zextras.modules.chat.server.dispatch;

import com.zextras.lib.Error.ZxError;
import com.zextras.modules.chat.server.address.SpecificAddress;

import java.util.Set;

public interface RoomServerHostSetProvider
{
  Set<String> getRoomServers();
  Set<String> getAllServers();
  Set<SpecificAddress> getRoomServersAddresses();
  Set<SpecificAddress> getAllServersAddresses();
  boolean isValidChatServer(SpecificAddress address);
  SpecificAddress selectNextServer() throws ZxError;
}
