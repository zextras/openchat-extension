/*
 * Copyright (C) 2017 ZeXtras S.r.l.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.zextras.modules.chat.utils;

import com.google.inject.Inject;
import org.openzal.zal.Account;
import org.openzal.zal.DistributionList;
import org.openzal.zal.Group;
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

  public boolean isDistributionList(String address)
  {
    DistributionList distributionList = null;
    try
    {
      distributionList = mProvisioning.getDistributionListById(address);
    }
    catch (Exception ignore) {}
    if (distributionList == null)
    {
      try
      {
        distributionList = mProvisioning.getDistributionListByName(address);
      }
      catch (Exception ignore) {}
    }
    return distributionList != null;
  }
}
