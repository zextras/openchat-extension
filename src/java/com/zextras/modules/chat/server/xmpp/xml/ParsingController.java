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

package com.zextras.modules.chat.server.xmpp.xml;

import org.xml.sax.InputSource;
import org.xml.sax.Locator;

public class ParsingController extends com.sun.msv.reader.util.IgnoreController
{
  private final SchemaProvider.SchemaLoader mSchemaLoader;
  public String mErrorMsg = "";

  public ParsingController(SchemaProvider.SchemaLoader schemaLoader)
  {
    mSchemaLoader = schemaLoader;
  }

  public void error(Locator[] locs, String msg, Exception nestedException)
  {
    if (mErrorMsg.isEmpty())
    {
      mErrorMsg = msg;
    }
    else
    {
      mErrorMsg += "; " + msg;
    }
  }

  @Override
  public InputSource resolveEntity(String publicId, String systemId)
  {
    //System.out.println("systemId: "+systemId);
    return mSchemaLoader.resolveEntity(publicId,systemId);
  }
}
