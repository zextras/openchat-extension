package com.zextras.modules.chat.server.exceptions;

import com.zextras.lib.Error.ErrorCode;

public class UnavailableResource extends ChatException
{
  public UnavailableResource(String address)
  {
    super("Unavailable resource "+address);
  }
}
