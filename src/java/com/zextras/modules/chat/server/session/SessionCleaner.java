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

package com.zextras.modules.chat.server.session;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.Runnable;
import com.zextras.lib.activities.ActivityManager;
import com.zextras.lib.activities.ActivityTimer;
import com.zextras.lib.log.ChatLog;
import org.openzal.zal.lib.Clock;
import com.zextras.lib.log.ZELogger;
import com.zextras.lib.switches.Service;
import com.zextras.modules.chat.server.events.EventManager;
import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.operations.SetStatusOnLoginOrLogout;
import com.zextras.modules.chat.server.status.FixedStatus;
import org.openzal.zal.Utils;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class SessionCleaner implements Service, ZELogger
{
  private final EventManager    mEventManager;
  private final SessionManager  mSessionManager;
  private final ActivityManager mActivityManager;
  private final Clock           mClock;
  private       ActivityTimer   mActivity;
  private final AtomicBoolean   mIsRunningAtomic;
  public final long INTERVAL = 15000L;

  @Inject
  public SessionCleaner(
    EventManager eventManager,
    SessionManager sessionManager,
    ActivityManager activityManager,
    Clock clock
  )
  {
    mEventManager = eventManager;
    mSessionManager = sessionManager;
    mActivityManager = activityManager;
    mClock = clock;
    mIsRunningAtomic = new AtomicBoolean(false);
    mActivity = null;
  }

  @Override
  public void start() throws ServiceStartException
  {
    mActivity = mActivityManager.scheduleActivityAtFixedRate(new CleanTask(), INTERVAL, INTERVAL);
  }

  @Override
  public void stop()
  {
    mActivity.cancel();
  }

  @Override
  public String getLoggerName()
  {
    return "Session Cleaner";
  }

  private class CleanTask implements Runnable
  {
    @Override
    public void run()
    {
      if( mIsRunningAtomic.compareAndSet(false, true) )
      {
        try
        {
          mSessionManager.cleanExpiredSessions(mClock, new SessionManager.SessionExpiredHandler()
          {
            @Override
            public void expired(Session session)
            {
              ChatOperation op = new SetStatusOnLoginOrLogout(session.getId(), FixedStatus.Offline);
              try
              {
                mEventManager.execOperations(Collections.singletonList(op));
              }
              catch (Exception ex)
              {
                ChatLog.log.debug(SessionCleaner.this, "Exception during cleanup: " + Utils.exceptionToString(ex));
              }
            }
          });
        }
        finally
        {
          mIsRunningAtomic.set(false);
        }
        //ChatLog.log.debug("Session Cleaner: "+mSessionManager.getAllSessions().toString());
      }
    }
  }
}
