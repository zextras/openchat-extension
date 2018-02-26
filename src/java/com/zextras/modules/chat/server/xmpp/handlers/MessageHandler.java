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

package com.zextras.modules.chat.server.xmpp.handlers;

import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.WritingState;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.operations.SendIsWriting;
import com.zextras.modules.chat.server.operations.SendMessage;
import com.zextras.modules.chat.server.xmpp.StanzaHandler;
import com.zextras.modules.chat.server.xmpp.XmppSession;
import com.zextras.modules.chat.server.xmpp.parsers.MessageParser;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.openzal.zal.lib.Clock;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class MessageHandler implements StanzaHandler
{
  private final XmppSession   mSession;
  private final Clock mClock;
  private       MessageParser mParser;

  public MessageHandler(
    XmppSession session,
    Clock clock
  )
  {
    mSession = session;
    mClock = clock;
  }

  @Override
  public List<ChatOperation> handle()
  {
    List<ChatOperation> eventList = new ArrayList(3);

    SpecificAddress targetAddress = new SpecificAddress(mParser.getTo());
    Target target = new Target( targetAddress );

    if( mParser.isComposing() )
    {
      ChatOperation sendWriting = new SendIsWriting(
        mSession.getExposedAddress(),
        target,
        WritingState.WRITING
      );
      eventList.add(sendWriting);
    }

    if( mParser.isGone() )
    {
      ChatOperation sendWriting = new SendIsWriting(
        mSession.getExposedAddress(),
        target,
        WritingState.GONE
      );
      eventList.add(sendWriting);
    }

    if( mParser.isActive() )
    {
      ChatOperation sendWriting = new SendIsWriting(
        mSession.getExposedAddress(),
        target,
        WritingState.STOPPED
      );
      eventList.add(sendWriting);
    }

    if( !mParser.getBody().isEmpty() )
    {
      String eventId = mParser.getId();
      if( eventId.isEmpty() ) {
        eventId = EventId.randomUUID().toString();
      }

      ChatOperation message = new SendMessage(
        EventId.fromString(eventId),
        mSession.getExposedAddress(),
        targetAddress,
        mParser.getBody(),
        mClock.now()
      );
      eventList.add(message);
    }

    return eventList;
  }

  @Override
  public void parse(ByteArrayInputStream xmlInputStream, SchemaProvider schemaProvider) throws XMLStreamException
  {
    mParser = new MessageParser(xmlInputStream, schemaProvider);
    mParser.parse();
  }
}
