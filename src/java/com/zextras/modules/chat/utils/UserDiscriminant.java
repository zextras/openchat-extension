package com.zextras.modules.chat.utils;

import com.google.inject.Inject;
import org.openzal.zal.Account;
import org.openzal.zal.Provisioning;

public class UserDiscriminant
{
  final private Provisioning mProvisioning;
  
  @Inject
  public UserDiscriminant(final Provisioning provisioning)
  {
    mProvisioning = provisioning;
  }
  
  public boolean isUser(String userAddress)
  {
    try
    {
      Account account = mProvisioning.getAccountByName(userAddress);
      if (account == null || account.isCalendarResource() || account.isIsSystemResource())
      {
        return false;
      }
      return true;
    }
    catch (Exception e)
    {
      return false;
    }
  }
}
