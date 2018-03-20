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

package com.zextras.modules.chat.server.interceptors;


import com.zextras.lib.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.properties.ChatProperties;
import com.zextras.modules.chat.server.ImMessage;
import com.zextras.modules.chat.server.address.AddressResolver;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.sql.ImMessageStatements;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventIQQuery;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.events.EventManager;
import com.zextras.modules.chat.server.events.EventMessage;
import com.zextras.modules.chat.server.events.EventMessageAck;
import com.zextras.modules.chat.server.events.EventMessageHistory;
import com.zextras.modules.chat.server.events.EventMessageHistoryLast;
import com.zextras.modules.chat.server.events.TargetType;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.history.HistoryMessageBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.openzal.zal.Account;
import org.openzal.zal.Provisioning;
import org.openzal.zal.Utils;
import org.openzal.zal.exceptions.ZimbraException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class QueryArchiveInterceptorFactoryImpl extends StubEventInterceptorFactory implements QueryArchiveInterceptorFactory
{
  private final Provisioning               mProvisioning;
  private final ChatProperties             mChatProperties;
  private final ImMessageStatements        mImMessageStatements;
  private final HistoryMessageBuilder      mImMessageBuilder;
  private final AddressResolver            mAddressResolver;
  private final Set<MessageHistoryFactory> mListeners;

  @Inject
  public QueryArchiveInterceptorFactoryImpl(
    Provisioning provisioning,
    ChatProperties chatProperties,
    ImMessageStatements imMessageStatements,
    HistoryMessageBuilder imMessageBuilder,
    AddressResolver addressResolver
  )
  {
    mProvisioning = provisioning;
    mChatProperties = chatProperties;
    mImMessageStatements = imMessageStatements;
    mImMessageBuilder = imMessageBuilder;
    mAddressResolver = addressResolver;
    mListeners = Collections.newSetFromMap(new ConcurrentHashMap<MessageHistoryFactory,Boolean>());
  }

  public void register(MessageHistoryFactory callback)
  {
    mListeners.add(callback);
  }

  public void unRegister(MessageHistoryFactory callback)
  {
    mListeners.remove(callback);
  }

  // Stored on sender host
  @Override
  public EventInterceptor interpret(final EventMessage eventMessage)
  {
    return new EventInterceptor()
    {
      @Override
      public boolean intercept(EventManager eventManager, SpecificAddress target) throws ChatException, ChatDbException, ZimbraException
      {
        final TargetType eventType = eventMessage.getType();
        if (eventType == TargetType.Chat)
        {
          String sender = eventMessage.getSender().withoutResource().toString();
          if (mChatProperties.isChatHistoryEnabled(sender))
          {
            Account account = mProvisioning.assertAccountByName(sender);
            if (mProvisioning.onLocalServer(account))
            {
              try
              {
                ImMessage message = new ImMessage(
                  eventMessage.getId().toString(),
                  sender,
                  target.withoutResource().toString(),
                  eventMessage.getMessage(),
                  TargetType.Chat,
                  eventMessage.getTimestamp());

                mImMessageStatements.insert(message);
              }
              catch (Exception e)
              {
                ChatLog.log.warn("Cannot save history for " + sender + ": " + Utils.exceptionToString(e));
              }
              return true;
            }
          }
        }
        return false;
      }
    };
  }

  // Stored on target host (that means: sender EventMessage server)
  @Override
  public EventInterceptor interpret(final EventMessageAck eventMessage)
  {
    return new EventInterceptor()
    {
      @Override
      public boolean intercept(EventManager eventManager, SpecificAddress target) throws ChatException, ChatDbException, ZimbraException
      {
        String sender = eventMessage.getSender().withoutResource().toString();
        try
        {
          Account account = mProvisioning.getAccountByName(eventMessage.getTarget().toSingleAddress());
          if (account != null && mProvisioning.onLocalServer(account))
          {
            mImMessageStatements.upsertMessageRead(
              sender,
              eventMessage.getTarget().toSingleAddress(),
              eventMessage.getMessageTimestamp(),
              eventMessage.getMessageId().toString()
            );
          }
        }
        catch (Exception e)
        {
          ChatLog.log.warn("Cannot save message_read for " + sender + ": " + Utils.exceptionToString(e));
        }
        return true;
      }
    };
  }

  @Override
  public EventInterceptor interpret(final EventIQQuery event) throws ChatException
  {
    return new EventInterceptor()
    {
      @Override
      public boolean intercept(final EventManager eventManager, SpecificAddress target) throws ChatException, ChatDbException, ZimbraException
      {
        if (mProvisioning.getLocalServer().getServerHostname().equals(target.withoutResource().toString()))
        {
          String queryId = event.getQueryId();
          final List<Event> events = query(
            event.getSender(),
            event.getNode().optValue(""),
            event.getWith().optValue(""),
            queryId,
            event.getStart(),
            event.getEnd(),
            event.getMax());

          eventManager.dispatchUnfilteredEvents(events);

          return true;
        }
        return false;
      }
    };
  }

  @Override
  public EventInterceptor interpret(final EventMessageHistory eventMessage)
  {
    return new EventInterceptor()
    {
      @Override
      public boolean intercept(EventManager eventManager, SpecificAddress target) throws ChatException, ChatDbException, ZimbraException
      {
        for (MessageHistoryFactory listener : mListeners)
        {
          listener.create(eventMessage);
        }
        return true;
      }
    };
  }

  @Override
  public EventInterceptor interpret(final EventMessageHistoryLast eventMessage)
  {
    return new EventInterceptor()
    {
      @Override
      public boolean intercept(EventManager eventManager, SpecificAddress target) throws ChatException, ChatDbException, ZimbraException
      {
        for (MessageHistoryFactory listener : mListeners)
        {
          listener.create(eventMessage);
        }
        return true;
      }
    };
  }

  private List<Event> query(
    SpecificAddress requester,
    String node,
    String with,
    String queryId,
    Optional<Long> start, Optional<Long> end, Optional<Integer> max
  ) throws ChatException
  {
    List<Event> events = new ArrayList<Event>(max.optValue(10));

    try
    {
      if (max.hasValue() && max.getValue() == 0) // Count only
      {
        Set<String> recipients = mImMessageStatements.getAllRecipients(with);
        for (String recipient : recipients)
        {
          Account account = mProvisioning.getAccountByName(recipient);
          if (account != null && mProvisioning.onLocalServer(account))
          {
            Pair<Long, String> pair = mImMessageStatements.getLastMessageRead(with, recipient);
            long timestamp = pair.getLeft();
            String messageId = pair.getRight();
            int count = mImMessageStatements.getCountMessageToRead(recipient, with, timestamp);
            events.add(new EventMessageHistoryLast(
              EventId.randomUUID(),
              new SpecificAddress(recipient),
              "",
              requester,
              "",
              messageId,
              Optional.<Integer>of(count),
              timestamp
            ));
            if (timestamp > 0L)
            {
              events.add(new EventMessageAck(
                new SpecificAddress(recipient),
                requester,
                EventId.fromString(messageId),
                timestamp
              ));
            }
          }
        }
      }
      else
      {
        SpecificAddress withAddress = new SpecificAddress( with );

        Optional<SpecificAddress> roomAddress = Optional.sEmptyInstance;
        if( mAddressResolver.tryResolveAddress(withAddress) != null ) {
          roomAddress = new Optional<>(withAddress);
        }

        Iterator<ImMessage> it = mImMessageStatements.query(node, with, start, end, max).iterator();
        String lastMessageId = "";
        String firstMessageId = "";
        int count = 0;
        while (it.hasNext())
        {
          ImMessage message = it.next();
          count++;
          lastMessageId = message.getId();
          if (firstMessageId.isEmpty())
          {
            firstMessageId = lastMessageId;
          }
          Event historyEvent = mImMessageBuilder.buildEvent(message, roomAddress);
          if (historyEvent != null)
          {
            events.add(new EventMessageHistory(
              EventId.randomUUID(),
              withAddress,
              queryId,
              requester,
              historyEvent
            ));
          }
        }

        events.add(new EventMessageHistoryLast(
          EventId.randomUUID(),
          withAddress,
          queryId,
          requester,
          firstMessageId,
          lastMessageId,
          Optional.of(count),
          System.currentTimeMillis()
        ));
      }
    }
    catch (SQLException e)
    {
      throw new ChatException(Utils.exceptionToString(e));
    }

    return events;
  }
}