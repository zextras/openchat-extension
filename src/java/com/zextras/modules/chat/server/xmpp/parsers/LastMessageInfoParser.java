/*
 * Copyright (C) 2018 ZeXtras S.r.l.
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

package com.zextras.modules.chat.server.xmpp.parsers;

import com.zextras.lib.Optional;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.stax2.XMLStreamReader2;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class LastMessageInfoParser extends XmppParser
{
  private String                       mEventId;
  private String                       mTimestamp;
  private String                       mTarget;
  private String                       mSender;
  private Optional<SpecificAddress>    mFinalDestination = Optional.sEmptyInstance;
  private Optional<Pair<Long, String>> mLastMessageSentInfo = Optional.sEmptyInstance;
  private Optional<Pair<Long, String>> mLastMessageReceivedInfo = Optional.sEmptyInstance;
  private Optional<Integer>            mUnreadCount = Optional.sEmptyInstance;
  private Optional<SpecificAddress>    mBuddyAddress = Optional.sEmptyInstance;

  public EventId getEventId()
  {
    return EventId.fromString(mEventId);
  }

  public Optional<Pair<Long, String>> getLastMessageSentInfo()
  {
    return mLastMessageSentInfo;
  }

  public Optional<Pair<Long, String>> getLastMessageReceivedInfo()
  {
    return mLastMessageReceivedInfo;
  }

  public Optional<SpecificAddress> getFinalDestination()
  {
    return mFinalDestination;
  }

  public Optional<Integer> getUnreadCount()
  {
    return mUnreadCount;
  }

  public SpecificAddress getSender()
  {
    return new SpecificAddress(mSender);
  }

  public Long getTimestamp()
  {
    return Long.valueOf(mTimestamp);
  }

  public SpecificAddress getTarget()
  {
    return new SpecificAddress(mTarget);
  }

  public Optional<SpecificAddress> getBuddyAddress()
  {
    return mBuddyAddress;
  }

  public LastMessageInfoParser(InputStream xmlInputStream, SchemaProvider schemaProvider)
  {
    super("", xmlInputStream, schemaProvider);
  }

  @Override
  public void parse() throws XMLStreamException
  {
    XMLStreamReader2 sr = getStreamReader();

    if (validate()) {
      sr.validateAgainst(getDefaultSchema());
    }

    while (sr.hasNext())
    {
      sr.next();
      switch (sr.getEventType()) {
        case XMLStreamReader.START_ELEMENT:
        {
          if (sr.getLocalName().equals("last_message_info")) {
            mEventId = emptyStringWhenNull(sr.getAttributeValue(null, "id"));
            mTimestamp = emptyStringWhenNull(sr.getAttributeValue(null, "timestamp"));
            mTarget = emptyStringWhenNull(sr.getAttributeValue(null, "target"));
            mSender = emptyStringWhenNull(sr.getAttributeValue(null, "sender"));
            String last_message_sent_id = emptyStringWhenNull(sr.getAttributeValue(null, "last_message_sent_id"));
            String last_message_sent_timestamp = emptyStringWhenNull(sr.getAttributeValue(null, "last_message_sent_timestamp"));
            if (!last_message_sent_id.isEmpty() && !last_message_sent_timestamp.isEmpty())
            {
              mLastMessageSentInfo = Optional.of(Pair.<Long, String>of(Long.valueOf(last_message_sent_timestamp), last_message_sent_id));
            }
            String last_message_received_id = emptyStringWhenNull(sr.getAttributeValue(null, "last_message_received_id"));
            String last_message_received_timestamp = emptyStringWhenNull(sr.getAttributeValue(null, "last_message_received_timestamp"));
            if (!last_message_received_id.isEmpty() && !last_message_received_timestamp.isEmpty())
            {
              mLastMessageReceivedInfo = Optional.of(Pair.<Long, String>of(Long.valueOf(last_message_received_timestamp), last_message_received_id));
            }
            String unread_count = emptyStringWhenNull(sr.getAttributeValue(null, "unread_count"));
            if (!unread_count.isEmpty())
            {
              mUnreadCount = Optional.of(Integer.valueOf(unread_count));
            }
            String finalDestination = emptyStringWhenNull(sr.getAttributeValue(null, "final_destination"));
            if (!finalDestination.isEmpty())
            {
              mFinalDestination = Optional.of(new SpecificAddress(finalDestination));
            }
            String buddyAddress = emptyStringWhenNull(sr.getAttributeValue(null, "buddy_address"));
            if (!buddyAddress.isEmpty())
            {
              mBuddyAddress = Optional.of(new SpecificAddress(buddyAddress));
            }
          }
          break;
        }
      }
    }
  }
}
