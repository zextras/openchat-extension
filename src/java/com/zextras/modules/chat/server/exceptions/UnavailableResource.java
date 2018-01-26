package com.zextras.modules.chat.server.exceptions;

import com.zextras.lib.Error.ErrorCode;

/**
 * Internal logical exception, when used there is an internal bug.
 */
public class UnavailableResource extends ChatException
{
  public UnavailableResource(String address)
  {
    super("Unavailable resource "+address);
  }
}
