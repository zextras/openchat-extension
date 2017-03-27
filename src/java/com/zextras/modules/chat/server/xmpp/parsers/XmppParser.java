/*
 * ZAL - The abstraction layer for Zimbra.
 * Copyright (C) 2017 ZeXtras S.r.l.
 *
 * This file is part of ZAL.
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
 * You should have received a copy of the GNU General Public License
 * along with ZAL. If not, see <http://www.gnu.org/licenses/>.
 */

package com.zextras.modules.chat.server.xmpp.parsers;

import com.ctc.wstx.stax.WstxInputFactory;
import com.zextras.modules.chat.server.xmpp.xml.SchemaProvider;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.validation.XMLValidationSchema;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

public abstract class XmppParser
{
  private final String         mSchemaName;
  private final InputStream    mXmlInput;
  private final SchemaProvider mSchemaProvider;

  protected XmppParser(
    String schemaName,
    InputStream xmlInput,
    SchemaProvider schemaProvider
  )
  {
    mSchemaName = schemaName;
    mXmlInput = xmlInput;
    mSchemaProvider = schemaProvider;
  }

  protected XMLValidationSchema getSchema(String name)
    throws XMLStreamException
  {
    return mSchemaProvider.getSchema(name);
  }

  protected XMLValidationSchema getDefaultSchema()
    throws XMLStreamException
  {
    return getSchema(mSchemaName);
  }

  protected XMLStreamReader2 getStreamReader() throws XMLStreamException
  {
    XMLInputFactory2 ifact = new WstxInputFactory();
    ifact.setProperty("javax.xml.stream.isCoalescing", true);
    XMLStreamReader2 sr = (XMLStreamReader2) ifact.createXMLStreamReader(mXmlInput);
    return sr;
  }

  protected boolean validate()
  {
    return false;
  }

  protected String emptyStringWhenNull(String value)
  {
    if (value == null) {
      return "";
    } else {
      return value;
    }
  }

  public abstract void parse() throws XMLStreamException;
}
