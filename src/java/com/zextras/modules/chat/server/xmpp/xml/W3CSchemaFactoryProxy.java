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

package com.zextras.modules.chat.server.xmpp.xml;

import com.ctc.wstx.msv.W3CSchema;
import com.ctc.wstx.msv.W3CSchemaFactory;
import com.sun.msv.grammar.xmlschema.XMLSchemaGrammar;
import com.sun.msv.reader.xmlschema.XMLSchemaReader;
import org.codehaus.stax2.validation.XMLValidationSchema;
import org.xml.sax.InputSource;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;

public class W3CSchemaFactoryProxy extends W3CSchemaFactory
{
  private ParsingController mParsingController = new ParsingController(new SchemaProvider.SchemaLoader());

  @Override
  protected XMLValidationSchema loadSchema(InputSource src, Object sysRef) throws XMLStreamException
  {
    SAXParserFactory saxFactory = getSaxFactory();
    XMLSchemaGrammar grammar = XMLSchemaReader.parse(src, saxFactory, mParsingController);
    if (grammar == null)
    {
      String msg = "Failed to load W3C Schema from '" + sysRef + "'";
      String emsg = mParsingController.mErrorMsg;
      msg += ": " + emsg;

      throw new XMLStreamException(msg);
    }
    return new W3CSchema(grammar);
  }
}
