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
import org.openzal.zal.lib.Clock;
import com.zextras.lib.switches.Service;
import com.zextras.modules.chat.server.Priority;
import com.zextras.modules.chat.server.address.ChatAddress;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.EventDestination;
import com.zextras.modules.chat.server.events.EventDestinationProvider;
import com.zextras.modules.chat.server.events.EventRouter;
import com.zextras.modules.chat.server.exceptions.NoSuchSessionException;
import com.zextras.lib.Visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Singleton
public class SessionManager implements EventDestinationProvider, Service
{
  private final Lock mLock = new ReentrantLock();
  private final Map<ChatAddress, List<Session>> mUserSessionMap;
  private final SortedSessionList               mSessionOrderedByUUIDList;
  private final EventRouter                     mEventRouter;
  private final Priority mPriority = new Priority(1);

  @Inject
  public SessionManager(EventRouter eventRouter)
  {
    mEventRouter = eventRouter;
    mUserSessionMap = new HashMap<ChatAddress, List<Session>>();
    mSessionOrderedByUUIDList = new SortedSessionList();
  }

  public void addSession(final Session session)
  {
    mLock.lock();
    try
    {
      final ChatAddress sessionAddress = session.getMainAddress().withoutResource();

      final List<Session> sessionList;

      if (mUserSessionMap.containsKey(sessionAddress))
      {
        sessionList = mUserSessionMap.get(sessionAddress);
      }
      else
      {
        sessionList = new ArrayList<Session>();
      }

      // TODO:  it should check if resource is just logged, if so return error or better terminate old session unfortunately with proxy auth no resource is sent
      if (!sessionList.contains(session))
      {
        sessionList.add(session);
        mSessionOrderedByUUIDList.add(session);
        mUserSessionMap.put(sessionAddress, sessionList);
      }
    }
    finally
    {
      mLock.unlock();
    }
  }

  public List<Session> getUserSessions(ChatAddress address)
  {
    mLock.lock();
    try
    {
      if(mUserSessionMap.containsKey(address.withoutResource()))
      {
        List<Session> sessions = mUserSessionMap.get(address.withoutResource());
        List<Session> rightSessions = new ArrayList<Session>();
        if (!address.resource().isEmpty())
        {
          for (Session session:sessions)
          {
            if (session.getExposedAddress().resource().equalsIgnoreCase(address.resource()))
            {
              rightSessions.add(session);
            }
          }
        }

        if (!rightSessions.isEmpty())
        {
          return rightSessions;
        }
        else
        {
          return new ArrayList<Session>(mUserSessionMap.get(address.withoutResource()));
        }
      }
      return Collections.emptyList();
    }
    finally
    {
      mLock.unlock();
    }
  }

  public interface SessionExpiredHandler
  {
    public void expired( Session session );
  }

  public void cleanExpiredSessions( Clock clock, SessionExpiredHandler sessionExpiredHandler )
  {
    ArrayList<Session> expiredSessions = new ArrayList<Session>(32);
    final long now = clock.now();

    mLock.lock();
    try
    {
      for( Session session : mSessionOrderedByUUIDList )
      {
        if( session.isExpired(now) ) {
          expiredSessions.add(session);
        }
      }
    }
    finally
    {
      mLock.unlock();
    }

    for( Session session : expiredSessions ) {
      sessionExpiredHandler.expired(session);
      terminateSessionById(session.getId());
    }
  }

  public List<Session> getAllSessions()
  {
    mLock.lock();
    try
    {
      return new ArrayList<Session>(mSessionOrderedByUUIDList);
    }
    finally
    {
      mLock.unlock();
    }
  }

  public Session getSessionById(SessionUUID sessionId)
    throws NoSuchSessionException
  {
    mLock.lock();
    try
    {
      return mSessionOrderedByUUIDList.getSessionById(sessionId);
    }
    finally
    {
      mLock.unlock();
    }
  }

  public void visitSessionById(SessionUUID sessionId, Visitor<Session> visitor)
  {
    Session session = null;

    mLock.lock();
    try
    {
      if( mSessionOrderedByUUIDList.hasSessionById(sessionId) )
      {
        try
        {
          session = mSessionOrderedByUUIDList.getSessionById(sessionId);
        }
        catch (NoSuchSessionException e)
        {
          throw new RuntimeException(e);
        }
      }
    }
    finally
    {
      mLock.unlock();
    }

    if( session != null )
    {
      visitor.visit(session);
    }
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder(512);

    for( ChatAddress address : mUserSessionMap.keySet() )
    {
      Collection<Session> sessionList = mUserSessionMap.get(address);
      sb.append("address: ").append(address.toString());
      for( Session session : sessionList ) {
        sb.append(session.getId().toString()).append(",");
      }
      sb.append("\n");
    }

    return sb.toString();
  }

  /**
   * Removes the session with the specified id.
   *
   * @return true if the session has been found and removed, false otherwise
   */
  public boolean terminateSessionById(SessionUUID sessionId)
  {
    mLock.lock();
    try
    {
      final Session sessionToRemove = getSessionById(sessionId);

      mSessionOrderedByUUIDList.remove(sessionToRemove);
      for(List<Session> sessionList : mUserSessionMap.values())
      {
        if(sessionList.remove(sessionToRemove))
        {
          break;
        }
      }

      sessionToRemove.expire();

      return true;
    }
    catch(NoSuchSessionException e)
    {
      return false;
    }
    finally
    {
      mLock.unlock();
    }
  }

  public void removeAccountSessions(SpecificAddress address)
  {
    List<Session> sessions = getUserSessions(address);
    for (Session session : sessions)
    {
      terminateSessionById(session.getId());
    }
  }

  @Override
  public boolean canHandle(SpecificAddress address)
  {
    return !getUserSessions(address).isEmpty();
  }

  @Override
  public Collection<? extends EventDestination> getDestinations(SpecificAddress address)
  {
    return getUserSessions(address);
  }

  @Override
  public Priority getPriority() {
    return mPriority;
  }

  public void clear()
  {
    mSessionOrderedByUUIDList.clear();
    mUserSessionMap.clear();
  }

  @Override
  public void start() throws ServiceStartException
  {
    mEventRouter.plugDestinationProvider(this);
  }

  @Override
  public void stop()
  {
    mEventRouter.unplugDestinationProvider(this);
  }
}
