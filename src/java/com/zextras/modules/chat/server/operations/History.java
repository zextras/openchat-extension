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

package com.zextras.modules.chat.server.operations;

import com.zextras.lib.sql.DbPrefetchIterator;
import com.zextras.modules.chat.server.ImMessage;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.db.sql.ImMessageStatements;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.events.EventMessage;
import com.zextras.modules.chat.server.events.EventMessageHistory;
import com.zextras.modules.chat.server.events.EventMessageHistoryLast;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.session.SessionManager;
import com.zextras.modules.chat.server.xmpp.XmppSession;
import com.zextras.modules.chat.server.xmpp.netty.StanzaProcessor;
import com.zextras.modules.chat.server.xmpp.parsers.IQQueryXmppParser;
import org.openzal.zal.lib.FakeClock;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class History implements ChatOperation
{
  private final StanzaProcessor.XmppConnectionHandler mXmppConnectionHandler;
  private final ImMessageStatements mMessageStatements;
  private final IQQueryXmppParser mParser;

  public History(
    StanzaProcessor.XmppConnectionHandler xmppConnectionHandler,
    ImMessageStatements messageStatements,
    IQQueryXmppParser parser
  )
  {
    mXmppConnectionHandler = xmppConnectionHandler;
    mMessageStatements = messageStatements;
    mParser = parser;
  }

  @Override
  public List<Event> exec(SessionManager sessionManager, UserProvider userProvider)
    throws ChatException, ChatDbException
  {
    List<Event> events = new ArrayList<Event>();
    XmppSession session = mXmppConnectionHandler.getSession();
    String connectedUser = session.getMainAddress().toString();
    String with = mParser.getWith();

    try
    {
      DbPrefetchIterator<ImMessage> it;
      if (with.isEmpty())
      {
        it = mMessageStatements.query(connectedUser);
      }
      else
      {
        it = mMessageStatements.query(connectedUser,with);
      }

      ImMessage lastMessage = null;
      ImMessage firstMessage = null;
      while (it.hasNext())
      {
        lastMessage = it.next();
        if (firstMessage == null)
        {
          firstMessage = lastMessage;
        }
        events.add(new EventMessageHistory(
          EventId.randomUUID(),
          mParser.getQueryId(),
          session.getMainAddress(),
          new EventMessage(
            EventId.fromString(lastMessage.getId()),
            new SpecificAddress(lastMessage.getSender()),
            new Target(new SpecificAddress(lastMessage.getDestination())),
            lastMessage.getText(),
            new FakeClock(lastMessage.getSentTimestamp())
          )
        ));
      }
      if (lastMessage != null)
      {
        events.add(new EventMessageHistoryLast(
          EventId.randomUUID(),
          mParser.getQueryId(),
          session.getMainAddress(),
          firstMessage.getId(),
          lastMessage.getId()
        ));
      }
    }
    catch (SQLException e)
    {
      throw new ChatException(e.getMessage());
    }

    return events;
  }
}
