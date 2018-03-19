package com.zextras.modules.chat.server.address;

import com.zextras.modules.chat.server.events.TargetType;
import org.openzal.zal.Provisioning;

import javax.inject.Inject;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * Conversion from and to chat subdomain.
 *
 * TODO:
 *  - avoid getAccountByName()
 *  - introduce configuration option to change the subdomain
 */
public class SubdomainResolver
{
  private final Provisioning mProvisioning;

  @Inject
  public SubdomainResolver(
    Provisioning provisioning
  )
  {
    mProvisioning = provisioning;
  }

  public String getSubdomainFor(String domain)
  {
    return "chat."+domain;
  }

  public String removeSubdomainFrom(TargetType eventType, String room)
  {
    if (eventType != TargetType.Chat)
    {
      return room.replace("@chat.", "@");
    }
    return room;
  }

  public String removeSubdomainFrom(String room)
  {
    if (!isValidAccountName(room))
    {
      return room.replace("@chat.", "@");
    }
    return room;
  }

  public SpecificAddress toRoomAddress(String room)
  {
    if (!isValidAccountName(room))
    {
      String[] strings = room.split("@");
      if (strings.length >= 2)
      {
        return new SpecificAddress(strings[0] + "@chat." + strings[1]);
      }
    }
    return new SpecificAddress(room);
  }

  public boolean isValidAccountName(String name)
  {
    return isValidEmail(name) && mProvisioning.getAccountByName(name) != null;
  }

  public boolean isValidEmail(String email)
  {
    boolean result = true;
    try
    {
      InternetAddress emailAddr = new InternetAddress(email);
      emailAddr.validate();
    }
    catch (AddressException ex)
    {
      result = false;
    }
    return result;
  }
}
