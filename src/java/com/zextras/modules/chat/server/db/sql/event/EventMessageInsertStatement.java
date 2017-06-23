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

package com.zextras.modules.chat.server.db.sql.event;

import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.sql.GenericInsertStatement;
import com.zextras.modules.chat.server.db.sql.SqlParameter;
import com.zextras.modules.chat.server.events.EventId;


public class EventMessageInsertStatement extends GenericInsertStatement {

  public EventMessageInsertStatement(final int userId,
                                     final EventId eventId,
                                     final SpecificAddress sender,
                                     final long timestamp,
                                     final String message)
  {
    super("chat.EVENTMESSAGE");
    mParameters.add(new SqlParameter<Integer>(1, "USERID", userId));
    mParameters.add(new SqlParameter<String>(2, "EVENTID", eventId.toString()));
    mParameters.add(new SqlParameter<String>(3, "SENDER", sender.toString()));
    mParameters.add(new SqlParameter<Long>(4, "TIMESTAMP", timestamp));
    mParameters.add(new SqlParameter<String>(5, "MESSAGE", message));
  }
}
