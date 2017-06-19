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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.codehaus.stax2.validation.XMLValidationSchema;
import org.codehaus.stax2.validation.XMLValidationSchemaFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

@Singleton
public class SchemaProvider
{
  private static final String SCHEMA_PREFIX            = "xmpp/schemas/";
  private static final String VALIDATOR                = "org.codehaus.stax2.validation.XMLValidationSchemaFactory.w3c";
  private static final String VALIDATOR_IMPLEMENTATION = "com.zextras.modules.chat.server.xmpp.xml.W3CSchemaFactoryProxy";
  private final XMLValidationSchemaFactory mValidationSchemaFactory;
  private final SchemaLoader               mSchemaLoader;

  static class SchemaLoader implements EntityResolver
  {
    @Override
    public InputSource resolveEntity(String publicId, String systemId)
    {
      final String filename;

      int idx = systemId.lastIndexOf('/');
      if (idx != -1)
      {
        filename = systemId.substring(idx+1);
      }
      else
      {
        filename = systemId;
      }

      if( filename == null || filename.isEmpty() ) {
        return null;
      }

      InputStream is = getClass().getClassLoader().getResourceAsStream(SCHEMA_PREFIX + filename);
      if( is != null ) {
        return new InputSource(is);
      }
      else {
        return null;
      }
    }
  }

  @Inject
  public SchemaProvider()
  {
    System.setProperty(VALIDATOR, VALIDATOR_IMPLEMENTATION);
    mValidationSchemaFactory = XMLValidationSchemaFactory.newInstance(XMLValidationSchema.SCHEMA_ID_W3C_SCHEMA, this.getClass().getClassLoader());
    mSchemaLoader = new SchemaLoader();
  }

  public XMLValidationSchema getSchema(String schemaName) throws XMLStreamException
  {
    return mValidationSchemaFactory.createSchema(getSchemaInputStream(schemaName));
  }

  public InputStream getSchemaInputStream(String schemaName)
  {
    InputSource source = mSchemaLoader.resolveEntity(null,schemaName);
    if (source == null) {
      throw new RuntimeException("Unable to find schema: " + schemaName);
    }
    return source.getByteStream();
  }


}
