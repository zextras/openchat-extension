package com.zextras.modules.chat.server.address;

import com.zextras.modules.chat.server.events.TargetType;
import org.openzal.zal.Account;
import org.openzal.zal.Provisioning;

import javax.inject.Inject;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * Conversion from and to chat subdomain.
 *
 */
public class ChatDbAddressConverter
{
  private final Provisioning mProvisioning;

  @Inject
  public ChatDbAddressConverter(
    Provisioning provisioning
  )
  {
    mProvisioning = provisioning;
  }

  public String fromDb(String domain)
  {
    return "__room."+domain;
  }

  public String toDb(String address)
  {
    Account account = null;
    try
    {
      account = mProvisioning.getAccountByName(address);
    }
    catch( Exception ignored ) {}
    if (account != null)
    {
      return account.getId();
    }
    return address.replace("@__room.", "@");
  }

  public SpecificAddress fromDb(TargetType eventType, String address)
  {
    if (!isValidEmail(address))
    {
      Account account = null;
      try
      {
        account = mProvisioning.getAccountById(address);
      }
      catch( Exception ignored )
      {
      }
      if( account != null )
      {
        address = account.getName();
      }
    }
    if (eventType != TargetType.Chat)
    {
      String[] strings = address.split("@");
      if (strings.length >= 2)
      {
        return new SpecificAddress(strings[0] + "@__room." + strings[1]);
      }
    }
    return new SpecificAddress(address);
  }

  private boolean isValidEmail(String email)
  {
    boolean result = true;
    try
    {
      InternetAddress emailAddr = new InternetAddress(email);
      emailAddr.validate();
    }
    catch ( AddressException ex)
    {
      result = false;
    }
    return result;
  }
}
