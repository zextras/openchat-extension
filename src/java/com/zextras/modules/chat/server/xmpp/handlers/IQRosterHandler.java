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

import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.operations.UpsertFriend;
import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.operations.GetRelationships;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.operations.RemoveFriend;
import com.zextras.modules.chat.server.xmpp.StanzaHandler;
import com.zextras.modules.chat.server.xmpp.XmppSession;
import com.zextras.modules.chat.server.xmpp.parsers.IQRequestType;
import com.zextras.modules.chat.server.xmpp.parsers.IQRosterParser;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.openzal.zal.Provisioning;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

public class IQRosterHandler implements StanzaHandler
{
  private final XmppSession  mSession;
  private final Provisioning mProvisioning;
  IQRosterParser mParser = null;

  public IQRosterHandler(
    XmppSession session,
    Provisioning provisioning
  )
  {
    mSession = session;
    mProvisioning = provisioning;
  }

  @Override
  public List<ChatOperation> handle()
  {
    String eventId = mParser.getRequestId();

    if (eventId.isEmpty())
    {
      eventId = EventId.randomUUID().toString();
    }

    if (mParser.getType().equals(IQRequestType.SET))
    {
      if (mParser.getSubscription().equals("remove"))
      {
        return Arrays.<ChatOperation>asList(
          new RemoveFriend(
            mSession.getExposedAddress(),
            new SpecificAddress(mParser.getJID()))
        );
      }
      else if (!mParser.getGroup().isEmpty())
      {
        SpecificAddress target = new SpecificAddress(mParser.getJID());

        return Arrays.<ChatOperation>asList(
          new UpsertFriend(
            mSession.getExposedAddress(),
            target,
            mParser.getItemName(),
            mParser.getGroup(),
            mProvisioning
          )
        );
      }
    }
    return Arrays.<ChatOperation>asList(
      new GetRelationships(
        EventId.fromString(eventId),
        mSession.getExposedAddress(),
        mSession.getId()
      )
    );
  }

  @Override
  public void parse(ByteArrayInputStream xmlInputStream, SchemaProvider schemaProvider) throws XMLStreamException
  {
    mParser = new IQRosterParser(xmlInputStream, schemaProvider);
    mParser.parse();
  }
}
