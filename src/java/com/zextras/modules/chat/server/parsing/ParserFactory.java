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

package com.zextras.modules.chat.server.parsing;

import com.google.inject.assistedinject.Assisted;
import com.zextras.lib.Optional;
import com.zextras.modules.chat.server.address.SpecificAddress;
import io.netty.channel.Channel;
import org.openzal.zal.soap.SoapResponse;
import org.openzal.zal.soap.ZimbraContext;

import java.util.Map;

public interface ParserFactory
{
  Parser create(
    @Assisted("senderAddress") SpecificAddress senderAddress,
    @Assisted("zimbraContext") Optional<ZimbraContext> zimbraContext,
    @Assisted("channel") Optional<Channel> channel,
    @Assisted("parameterMap") Map<String, String> parameterMap,
    @Assisted("soapResponse") SoapResponse soapResponse
  );
}
