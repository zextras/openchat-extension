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

import com.ctc.wstx.msv.W3CSchema;
import com.ctc.wstx.msv.W3CSchemaFactory;
import com.sun.msv.grammar.xmlschema.XMLSchemaGrammar;
import com.sun.msv.reader.xmlschema.XMLSchemaReader;
import org.codehaus.stax2.validation.XMLValidationSchema;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;

public class W3CSchemaFactoryProxy extends W3CSchemaFactory
{
  private ParsingController mParsingController = new ParsingController(new SchemaProvider.SchemaLoader());

  @Override
  protected XMLValidationSchema loadSchema(InputSource src, Object sysRef) throws XMLStreamException
  {
    SAXParserFactory saxFactory = getSaxFactory();
    try
    {
      saxFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      saxFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      saxFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    }
    catch (SAXNotRecognizedException e)
    {
      throw new RuntimeException(e);
    }
    catch (SAXNotSupportedException e)
    {
      throw new RuntimeException(e);
    }
    catch (ParserConfigurationException e)
    {
      throw new RuntimeException(e);
    }

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
