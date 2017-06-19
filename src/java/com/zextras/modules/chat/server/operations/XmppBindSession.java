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

import com.zextras.modules.chat.server.db.providers.UserProvider;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.session.SessionManager;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.address.SpecificAddressFromSession;
import com.zextras.modules.chat.server.events.EventBindResult;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.xmpp.XmppSession;
import com.zextras.modules.chat.server.xmpp.parsers.BindParser;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class XmppBindSession implements ChatOperation
{
  private final BindParser mParser;
  private final XmppSession mSession;

  public XmppBindSession(BindParser parser, XmppSession session) {
    mParser = parser;
    mSession = session;
  }


  @Override
  public List<Event> exec(SessionManager sessionManager, UserProvider userProvider) throws ChatException, ChatDbException
  {
    SpecificAddress mainAddress = mSession.getMainAddress();
    String resource = mParser.getResource();
    if( resource.isEmpty() ) {
      resource = UUID.randomUUID().toString();
    }

    SpecificAddressFromSession addressWithResource = new SpecificAddressFromSession(
      mainAddress.toString(),
      resource,
      mSession.getId()
    );

    mSession.setMainAddress(addressWithResource);
    mSession.setExposedAddress(addressWithResource);

    Event eventBindResult = new EventBindResult(
      EventId.fromString(mParser.getBindId()),
      mSession.getMainAddress()
    );
    mSession.getEventQueue().queueEvent(eventBindResult);

    return Collections.emptyList();
  }
}
