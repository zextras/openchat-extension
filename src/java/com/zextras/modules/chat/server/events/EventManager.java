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

package com.zextras.modules.chat.server.events;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.zextras.lib.Error.ZxError;
import com.zextras.lib.filters.FilterPassAll;
import com.zextras.lib.filters.FilteredIterator;
import com.zextras.lib.log.ChatLog;
import com.zextras.lib.switches.Service;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import org.openzal.zal.lib.Filter;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.session.SessionManager;
import org.openzal.zal.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Singleton
public class EventManager implements Service
{
  private final UserProvider   mOpenUserProvider;
  private final SessionManager mSessionManager;
  private final EventRouter    mEventRouter;
  //private final ChatReportManager mChatReportManager;

  @Inject
  public EventManager(
    UserProvider openUserProvider,
    SessionManager sessionManager,
    EventRouter eventRouter/*,
    ChatReportManager chatReportManager*/)
  {
    mOpenUserProvider = openUserProvider;
    mSessionManager = sessionManager;
    mEventRouter = eventRouter;
    //mChatReportManager = chatReportManager;
  }

  public void execOperations(List<ChatOperation> chatOperations, Filter<Event> outEventFilter)
    throws ChatException, ChatDbException
  {
    ArrayList<Event> events = new ArrayList<Event>(16);

    for (ChatOperation operation : chatOperations)
    {
      try
      {
        events.addAll(operation.exec(mSessionManager, mOpenUserProvider));
      }
      catch (ChatException e)
      {
        //mChatReportManager.doReport(e, operation);
        throw e;
      }
    }

    Iterator<Event> it = createIterator(events, outEventFilter);
    dispatchEvents(it);
  }

  private void dispatchEvent(Event event)
  {
    try
    {
      event.getTarget().dispatch(mEventRouter, mOpenUserProvider, event);
    }
    catch (ZxError e)
    {
      ChatLog.log.crit("EventManager unable to dispatch event: " +event.getClass().getName() + " " +
              "Exception: " + Utils.exceptionToString(e));
      //mChatReportManager.doReport(e, event);
    }
  }

  private void dispatchEvents(Iterator<Event> it)
  {
    while( it.hasNext() )
    {
      dispatchEvent(it.next());
    }
  }

  public void dispatchUnfilteredEvent(Event event)
  {
    dispatchEvent( event );
  }

  public void dispatchUnfilteredEvents(List<Event> it)
  {
    dispatchEvents( it.iterator() );
  }

  private Iterator<Event> createIterator(ArrayList<Event> events, Filter<Event> outEventFilter)
  {
    return new FilteredIterator<Event>(
      outEventFilter,
      events.iterator()
    );
  }

  public void execOperations(List<ChatOperation> chatOperations)
    throws ChatException, ChatDbException
  {
    execOperations(chatOperations,new FilterPassAll<Event>());
  }

  @Override
  public void start() throws ServiceStartException
  {

  }

  @Override
  public void stop()
  {
    mEventRouter.stop();
  }
}
