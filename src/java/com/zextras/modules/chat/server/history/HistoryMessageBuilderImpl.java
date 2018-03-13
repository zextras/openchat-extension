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

package com.zextras.modules.chat.server.history;

import com.zextras.lib.Optional;
import com.zextras.modules.chat.server.ImMessage;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.events.EventMessage;

public class HistoryMessageBuilderImpl implements HistoryMessageBuilder
{
  @Override
  public Event buildEvent(ImMessage message, Optional<SpecificAddress> roomAddress)
  {
    switch (message.getEventType())
    {
      case Message:
      {
        SpecificAddress sender;
        if (roomAddress.hasValue())
        {
          sender = new SpecificAddress(
            roomAddress.toString(),
            message.getSender()
          );
        }
        else
        {
          sender = new SpecificAddress(message.getSender());
        }

        return new EventMessage(
          EventId.fromString(message.getId()),
          sender,
          new Target(new SpecificAddress(message.getDestination())),
          message.getText(),
          message.getSentTimestamp(),
          message.getTargetType()
        );
      }
    }

    return null;
  }
}
