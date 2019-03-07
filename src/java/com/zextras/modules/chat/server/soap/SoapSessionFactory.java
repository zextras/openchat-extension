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

package com.zextras.modules.chat.server.soap;


import com.zextras.lib.Optional;
import com.zextras.modules.chat.server.User;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventQueue;
import com.zextras.modules.chat.server.filters.EventFilter;
import com.zextras.modules.chat.server.session.SessionUUID;
import com.zextras.modules.chat.server.session.SoapEventFilter;
import org.openzal.zal.lib.Filter;
import org.openzal.zal.lib.Version;

public interface SoapSessionFactory {
  public SoapSession create(SessionUUID id,
                            EventQueue eventQueue,
                            User user,
                            SpecificAddress address,
                            Version clientVersion,
                            EventFilter soapEventFilter
    );
}
