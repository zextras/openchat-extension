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

import com.zextras.modules.chat.server.Relationship;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.EventFriendAccepted;
import com.zextras.modules.chat.server.events.EventFriendAdded;
import com.zextras.modules.chat.server.events.EventInterpreter;
import com.zextras.modules.chat.server.events.EventManager;
import com.zextras.modules.chat.server.events.EventMessage;
import com.zextras.modules.chat.server.events.EventStatusChanged;
import com.zextras.modules.chat.server.events.EventStatusProbe;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.interceptors.EventInterceptor;
import com.zextras.modules.chat.server.interceptors.StubEventInterceptorFactory;
import org.openzal.zal.exceptions.ZimbraException;

import java.util.concurrent.atomic.AtomicBoolean;

public class CommonSessionEventInterceptorBuilderImpl implements CommonSessionEventInterceptorBuilder
{
  @Override
  public EventInterpreter<EventInterceptor> buildFactory(final Session session, final AtomicBoolean settableBoolean)
  {
    return new StubEventInterceptorFactory()
    {
      public EventInterceptor interpret(EventFriendAdded eventFriendAdded)
      {
        return buildInterceptor(eventFriendAdded, settableBoolean);
      }

      public EventInterceptor interpret(EventStatusChanged setStatus)
      {
        return buildInterceptor(setStatus, settableBoolean, session);
      }

      public EventInterceptor interpret(EventMessage eventMessage)
      {
        return buildInterceptor(eventMessage, settableBoolean, session);
      }

      public EventInterceptor interpret(EventStatusProbe eventStatusProbe)
      {
        return buildInterceptor(eventStatusProbe, settableBoolean);
      }

      public EventInterceptor interpret(EventFriendAccepted eventFriendAccepted)
      {
        return buildInterceptor(eventFriendAccepted, settableBoolean, session);
      }
    };
  }

  public EventInterceptor buildInterceptor(EventFriendAdded eventFriendAdded, AtomicBoolean settableBoolean)
  {
    return new FilterEventInterceptor(
      settableBoolean,
      true
    );
  }

  public EventInterceptor buildInterceptor(EventStatusChanged setStatus, AtomicBoolean settableBoolean, Session session)
  {
    return new FilterIfDifferentRelationship(
      settableBoolean,
      setStatus.getSender(),
      session,
      Relationship.RelationshipType.ACCEPTED
    );
  }

  public EventInterceptor buildInterceptor(EventMessage eventMessage, AtomicBoolean settableBoolean, Session session)
  {
    return new FilterIfDifferentRelationship(
      settableBoolean,
      eventMessage.getSender(),
      session,
      Relationship.RelationshipType.ACCEPTED
    );
  }

  public EventInterceptor buildInterceptor(EventStatusProbe eventStatusProbe, AtomicBoolean settableBoolean)
  {
    return new FilterEventInterceptor(
      settableBoolean,
      true
    );
  }

  public EventInterceptor buildInterceptor(EventFriendAccepted eventFriendAccepted, AtomicBoolean settableBoolean, Session session)
  {
    return new FilterIfDifferentRelationship(
      settableBoolean,
      eventFriendAccepted.getSender(),
      session,
      Relationship.RelationshipType.INVITED,
      Relationship.RelationshipType.ACCEPTED
    );
  }

  private static class FilterIfDifferentRelationship implements EventInterceptor
  {
    private final AtomicBoolean                   mSettableBoolean;
    private final SpecificAddress                 mAddress;
    private final Session                         mSession;
    private final Relationship.RelationshipType[] mRelationshipTypes;

    public FilterIfDifferentRelationship(
      AtomicBoolean settableBoolean,
      SpecificAddress address,
      Session session,
      Relationship.RelationshipType ... relationshipTypes
    )
    {
      mSettableBoolean = settableBoolean;
      mAddress = address;
      mSession = session;
      mRelationshipTypes = relationshipTypes;
    }

    @Override
    public void intercept(EventManager eventManager, SpecificAddress target)
      throws ChatException, ChatDbException, ZimbraException
    {
      if( target.equals(mAddress) ){
        return;
      }

      for(Relationship.RelationshipType allowedType : mRelationshipTypes )
      {
        if( mSession.getUser().hasRelationshipType(mAddress,allowedType) ) {
          return;
        }
      }
      mSettableBoolean.set(true);
    }
  }
}
