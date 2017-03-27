/*
 * ZAL - The abstraction layer for Zimbra.
 * Copyright (C) 2017 ZeXtras S.r.l.
 *
 * This file is part of ZAL.
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
 * You should have received a copy of the GNU General Public License
 * along with ZAL. If not, see <http://www.gnu.org/licenses/>.
 */

package com.zextras.modules.chat.server.session;

import com.zextras.modules.chat.server.exceptions.NoSuchSessionException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class SortedSessionList extends ArrayList<Session>
{
  @Override
  public boolean add(Session e)
  {
    final int index = Collections.binarySearch(this, e.getId());

    if(index >= 0)
    {
      set(index, e);
      return true;
    }
    else
    {
      final int insertionPoint = - index - 1;
      add(insertionPoint, e);
    }

    return true;
  }

  @Override
  public boolean addAll(int index, Collection<? extends Session> c)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(Collection<? extends Session> c)
  {
    for(Session session : c)
    {
      add(session);
    }

    return true;
  }

  public boolean hasSessionById(SessionUUID sessionId)
  {
    int index = Collections.binarySearch(this, sessionId);

    if(index >= 0)
    {
      return true;
    }

    return false;
  }

  public Session getSessionById(SessionUUID sessionId)
    throws NoSuchSessionException
  {
    int index = Collections.binarySearch(this, sessionId);

    if(index >= 0)
    {
      return get(index);
    }

    throw new NoSuchSessionException(sessionId);
  }
}
