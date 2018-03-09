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

package com.zextras.modules.chat.server.xmpp.decoders;

import com.zextras.modules.chat.server.relationship.Relationship;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.User;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.events.*;
import com.zextras.modules.chat.server.status.FixedStatus;
import com.zextras.modules.chat.server.status.Status;
import com.zextras.modules.chat.server.status.VolatileStatus;
import com.zextras.modules.chat.server.xmpp.parsers.PresenceParser;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.openzal.zal.Provisioning;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PresenceDecoder implements EventDecoder
{
  private final SchemaProvider mSchemaProvider;
  private final Provisioning   mProvisioning;
  private final UserProvider   mOpenUserProvider;

  public static final HashMap<String, Status.StatusType> sShowToStatusMap;

  static
  {
    sShowToStatusMap = new HashMap<String, Status.StatusType>(4);
    sShowToStatusMap.put("chat", Status.StatusType.AVAILABLE);
    sShowToStatusMap.put("away", Status.StatusType.AWAY);
    sShowToStatusMap.put("xa", Status.StatusType.AWAY);
    sShowToStatusMap.put("dnd", Status.StatusType.BUSY);
  }

  public PresenceDecoder(SchemaProvider schemaProvider, Provisioning provisioning, UserProvider openUserProvider)
  {
    mSchemaProvider = schemaProvider;
    mProvisioning = provisioning;
    mOpenUserProvider = openUserProvider;
  }

  @Override
  public List<Event> decode(InputStream inputStream)
    throws XMLStreamException
  {

    PresenceParser parser = new PresenceParser(inputStream, mSchemaProvider);
    parser.parse();

    SpecificAddress targetAddress = new SpecificAddress(parser.getTo());
    SpecificAddress sender = new SpecificAddress(parser.getFrom());
    Target target = new Target(targetAddress);

    if (parser.getType().equals("subscribed"))
    {
      String buddyNickname = getFriendNickname(sender, targetAddress);

      EventFriendAccepted eventFriendAccepted = new EventFriendAccepted(
        sender,
        targetAddress,
        target
      );

      return Arrays.<Event>asList(
        eventFriendAccepted
      );
    }

    if (parser.getType().equals("subscribe"))
    {
      return Arrays.<Event>asList(
        new EventFriendAdded(
          sender,
          targetAddress
        )
      );
    }

    if (parser.getType().equals("unsubscribe"))
    {
      //nothing reported to the other side
    }

    if( parser.getType().equals("unavailable") || parser.getType().equals("invisible") )
    {
      Status newStatus = new FixedStatus(Status.StatusType.OFFLINE);
      return Arrays.<Event>asList(
        new EventStatusChanged(
          //EventId.fromString(parser.getId())
          sender,
          target,
          newStatus,
          parser.getGroupType()
        )
      );
    }

    if (parser.isMucPresence())
    {
      Status status;
      if ("unavailable".equals(parser.getType()))
      {
        status = FixedStatus.Offline;
      }
      else
      {
        if (sShowToStatusMap.containsKey(parser.getShow()))
        {
          status = new VolatileStatus(sShowToStatusMap.get(parser.getShow().toLowerCase()), parser.getStatusText());
        }
        else
        {
          status = FixedStatus.Available;
        }
      }
      return Collections.<Event>singletonList(
        new EventStatusChanged(
          sender,
          target,
          status,
          TargetType.GroupChat
        )
      );
    }

    if( parser.getType().isEmpty() )
    {
      Status.StatusType type = sShowToStatusMap.get(parser.getShow().toLowerCase());
      if( type == null ) {
        type = Status.StatusType.valueOf(parser.getShow().toUpperCase());
      }

      Status newStatus = new VolatileStatus(
        type,
        parser.getStatusText(),
        parser.getValidSince(),
        parser.getMeetings()
      );

      return Arrays.<Event>asList(
        new EventStatusChanged(
          sender,
          target,
          newStatus,
          parser.getGroupType()
        )
      );
    }

    return Collections.emptyList();
  }

  private String getFriendNickname(SpecificAddress sender, SpecificAddress targetAddress) {
    try {
      User senderUser = mOpenUserProvider.getUser(sender);
      Relationship relationship = senderUser.getRelationship(targetAddress);
      return relationship.getBuddyNickname();
    }
    catch (Exception ex) {
      return sender.toString();
    }
  }
}
