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

package com.zextras.modules.chat.server.destinations;

import com.google.inject.Inject;
import com.zextras.lib.activities.ActivityManager;
import com.zextras.modules.chat.server.DestinationQueue;
import com.zextras.modules.chat.server.EventSender;
import com.zextras.modules.chat.server.EventSenderImpl;
import com.zextras.modules.chat.server.LocalXmppConnectionProvider;
import org.openzal.zal.Provisioning;

public class EventSenderFactory
{
  private final LocalXmppConnectionProvider mLocalXmppConnectionProvider;
  private final int mSteps;
  private final ActivityManager mActivityManager;

  @Inject
  public EventSenderFactory(
    LocalXmppConnectionProvider localXmppConnectionProvider,
    ActivityManager activityManager
  )
  {
    this(activityManager, localXmppConnectionProvider, 0);
  }

  public EventSenderFactory(
    ActivityManager activityManager,
    LocalXmppConnectionProvider localXmppConnectionProvider,
    int steps
  )
  {
    mLocalXmppConnectionProvider = localXmppConnectionProvider;
    mSteps = steps;
    mActivityManager = activityManager;
  }

  public EventSender createEventSender(DestinationQueue destinationQueue)
  {
    return new EventSenderImpl(
      mActivityManager,
      mLocalXmppConnectionProvider,
      destinationQueue,
      mSteps
    );
  }
}
