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

import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.events.EventLastMessageInfo;
import com.zextras.modules.chat.server.relationship.Relationship;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventFriendAccepted;
import com.zextras.modules.chat.server.events.EventFriendAdded;
import com.zextras.modules.chat.server.events.EventInterpreterAdapter;
import com.zextras.modules.chat.server.events.EventMessage;
import com.zextras.modules.chat.server.events.EventStatusChanged;
import com.zextras.modules.chat.server.events.EventStatusProbe;
import com.zextras.modules.chat.server.filters.EventFilter;

public class CommonSessionEventFilterImpl implements CommonSessionEventFilter
{
  @Override
  public boolean isFiltered(Event event, SpecificAddress target, Session session) throws ChatException
  {
    return event.interpret(
      new Interpreter(target, session)
    );
  }

  class Interpreter extends EventInterpreterAdapter<Boolean>
  {
    private final SpecificAddress mTarget;
    private final Session mSession;

    Interpreter(SpecificAddress target, Session session)
    {
      super(false);
      mTarget = target;
      mSession = session;
    }

    @Override
    public Boolean interpret(EventFriendAdded eventFriendAdded)
    {
      return true;
    }

    @Override
    public Boolean interpret(EventStatusProbe eventStatusProbe)
    {
      return true;
    }

    @Override
    public Boolean interpret(EventStatusChanged eventStatusChanged)
    {
      return filterIfDifferentRelationship(
        eventStatusChanged.getSender(),
        Relationship.RelationshipType.ACCEPTED
      );
    }

    @Override
    public Boolean interpret(EventMessage eventMessage)
    {
      return filterIfDifferentRelationship(
        eventMessage.getSender(),
        Relationship.RelationshipType.ACCEPTED
      );
    }

    @Override
    public Boolean interpret(EventFriendAccepted eventFriendAccepted)
    {
      return filterIfDifferentRelationship(
        eventFriendAccepted.getSender(),
        Relationship.RelationshipType.INVITED,
        Relationship.RelationshipType.ACCEPTED
      );
    }

    @Override
    public Boolean interpret(EventLastMessageInfo event)
    {
      return !event.isRequired();
    }

    private boolean filterIfDifferentRelationship(
      SpecificAddress sender,
      Relationship.RelationshipType... allowedRelationshipTypes
    )
    {
      if (mTarget.equals(sender))
      {
        return false;
      }

      try
      {
        Relationship relationship = mSession.getUser().getRelationship(sender);
        if (relationship == null)
        {
          return false;
        }
        for (Relationship.RelationshipType allowedType : allowedRelationshipTypes)
        {
          if (relationship.getType() == allowedType)
          {
            return false;
          }
        }
      }
      catch (RuntimeException e)
      {
        return false;
      }

      return true;
    }
  }
}
