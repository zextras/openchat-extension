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

package com.zextras.modules.chat.server.xmpp;

import com.ctc.wstx.stax.WstxInputFactory;
import org.codehaus.stax2.AttributeInfo;
import org.codehaus.stax2.DTDInfo;
import org.codehaus.stax2.LocationInfo;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.typed.Base64Variant;
import org.codehaus.stax2.typed.TypedArrayDecoder;
import org.codehaus.stax2.typed.TypedValueDecoder;
import org.codehaus.stax2.validation.ValidationProblemHandler;
import org.codehaus.stax2.validation.XMLValidationSchema;
import org.codehaus.stax2.validation.XMLValidator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;

public class RewindableXmlStreamReader implements XMLStreamReader2
{
  private final String mXml;
  private XMLStreamReader2 mXMLStreamReader;

  public RewindableXmlStreamReader(String xml) throws XMLStreamException
  {
    mXml = xml;
    mXMLStreamReader = getStreamReader(xml);
  }

  public void rewind() throws XMLStreamException
  {
    mXMLStreamReader = getStreamReader(mXml);
  }

  private InputStream getInputStream(String xml)
  {
    try
    {
      return new ByteArrayInputStream(xml.getBytes("UTF-8"));
    }
    catch (UnsupportedEncodingException e)
    {
      throw new RuntimeException("unsupported utf-8?");
    }
  }

  private XMLStreamReader2 getStreamReader(String xml) throws XMLStreamException
  {
    XMLInputFactory2 ifact = new WstxInputFactory();
    ifact.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    ifact.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
    XMLStreamReader2 sr = (XMLStreamReader2) ifact.createXMLStreamReader(getInputStream(xml));
    return sr;
  }

  @Override
  public boolean isPropertySupported(String name)
  {
    return mXMLStreamReader.isPropertySupported(name);
  }

  @Override
  public boolean setProperty(String name, Object value)
  {
    return mXMLStreamReader.setProperty(name, value);
  }

  @Override
  public Object getFeature(String name)
  {
    return mXMLStreamReader.getFeature(name);
  }

  @Override
  public void setFeature(String name, Object value)
  {
    mXMLStreamReader.setFeature(name, value);
  }

  @Override
  public void skipElement() throws XMLStreamException
  {
    mXMLStreamReader.skipElement();
  }

  @Override
  public DTDInfo getDTDInfo() throws XMLStreamException
  {
    return mXMLStreamReader.getDTDInfo();
  }

  @Override
  public AttributeInfo getAttributeInfo() throws XMLStreamException
  {
    return mXMLStreamReader.getAttributeInfo();
  }

  @Override
  public LocationInfo getLocationInfo()
  {
    return mXMLStreamReader.getLocationInfo();
  }

  @Override
  public int getText(Writer w, boolean preserveContents) throws IOException, XMLStreamException
  {
    return mXMLStreamReader.getText(w, preserveContents);
  }

  @Override
  public boolean isEmptyElement() throws XMLStreamException
  {
    return mXMLStreamReader.isEmptyElement();
  }

  @Override
  public int getDepth()
  {
    return mXMLStreamReader.getDepth();
  }

  @Override
  public NamespaceContext getNonTransientNamespaceContext()
  {
    return mXMLStreamReader.getNonTransientNamespaceContext();
  }

  @Override
  public String getPrefixedName()
  {
    return mXMLStreamReader.getPrefixedName();
  }

  @Override
  public void closeCompletely() throws XMLStreamException
  {
    mXMLStreamReader.closeCompletely();
  }

  @Override
  public boolean getElementAsBoolean() throws XMLStreamException
  {
    return mXMLStreamReader.getElementAsBoolean();
  }

  @Override
  public int getElementAsInt() throws XMLStreamException
  {
    return mXMLStreamReader.getElementAsInt();
  }

  @Override
  public long getElementAsLong() throws XMLStreamException
  {
    return mXMLStreamReader.getElementAsLong();
  }

  @Override
  public float getElementAsFloat() throws XMLStreamException
  {
    return mXMLStreamReader.getElementAsFloat();
  }

  @Override
  public double getElementAsDouble() throws XMLStreamException
  {
    return mXMLStreamReader.getElementAsDouble();
  }

  @Override
  public BigInteger getElementAsInteger() throws XMLStreamException
  {
    return mXMLStreamReader.getElementAsInteger();
  }

  @Override
  public BigDecimal getElementAsDecimal() throws XMLStreamException
  {
    return mXMLStreamReader.getElementAsDecimal();
  }

  @Override
  public QName getElementAsQName() throws XMLStreamException
  {
    return mXMLStreamReader.getElementAsQName();
  }

  @Override
  public byte[] getElementAsBinary() throws XMLStreamException
  {
    return mXMLStreamReader.getElementAsBinary();
  }

  @Override
  public byte[] getElementAsBinary(Base64Variant variant) throws XMLStreamException
  {
    return mXMLStreamReader.getElementAsBinary(variant);
  }

  @Override
  public void getElementAs(TypedValueDecoder tvd) throws XMLStreamException
  {
    mXMLStreamReader.getElementAs(tvd);
  }

  @Override
  public int readElementAsBinary(byte[] resultBuffer, int offset, int maxLength, Base64Variant variant) throws XMLStreamException
  {
    return mXMLStreamReader.readElementAsBinary(resultBuffer, offset, maxLength, variant);
  }

  @Override
  public int readElementAsBinary(byte[] resultBuffer, int offset, int maxLength) throws XMLStreamException
  {
    return mXMLStreamReader.readElementAsBinary(resultBuffer, offset, maxLength);
  }

  @Override
  public int readElementAsIntArray(int[] resultBuffer, int offset, int length) throws XMLStreamException
  {
    return mXMLStreamReader.readElementAsIntArray(resultBuffer, offset, length);
  }

  @Override
  public int readElementAsLongArray(long[] resultBuffer, int offset, int length) throws XMLStreamException
  {
    return mXMLStreamReader.readElementAsLongArray(resultBuffer, offset, length);
  }

  @Override
  public int readElementAsFloatArray(float[] resultBuffer, int offset, int length) throws XMLStreamException
  {
    return mXMLStreamReader.readElementAsFloatArray(resultBuffer, offset, length);
  }

  @Override
  public int readElementAsDoubleArray(double[] resultBuffer, int offset, int length) throws XMLStreamException
  {
    return mXMLStreamReader.readElementAsDoubleArray(resultBuffer, offset, length);
  }

  @Override
  public int readElementAsArray(TypedArrayDecoder tad) throws XMLStreamException
  {
    return mXMLStreamReader.readElementAsArray(tad);
  }

  @Override
  public int getAttributeIndex(String namespaceURI, String localName)
  {
    return mXMLStreamReader.getAttributeIndex(namespaceURI, localName);
  }

  @Override
  public boolean getAttributeAsBoolean(int index) throws XMLStreamException
  {
    return mXMLStreamReader.getAttributeAsBoolean(index);
  }

  @Override
  public int getAttributeAsInt(int index) throws XMLStreamException
  {
    return mXMLStreamReader.getAttributeAsInt(index);
  }

  @Override
  public long getAttributeAsLong(int index) throws XMLStreamException
  {
    return mXMLStreamReader.getAttributeAsLong(index);
  }

  @Override
  public float getAttributeAsFloat(int index) throws XMLStreamException
  {
    return mXMLStreamReader.getAttributeAsFloat(index);
  }

  @Override
  public double getAttributeAsDouble(int index) throws XMLStreamException
  {
    return mXMLStreamReader.getAttributeAsDouble(index);
  }

  @Override
  public BigInteger getAttributeAsInteger(int index) throws XMLStreamException
  {
    return mXMLStreamReader.getAttributeAsInteger(index);
  }

  @Override
  public BigDecimal getAttributeAsDecimal(int index) throws XMLStreamException
  {
    return mXMLStreamReader.getAttributeAsDecimal(index);
  }

  @Override
  public QName getAttributeAsQName(int index) throws XMLStreamException
  {
    return mXMLStreamReader.getAttributeAsQName(index);
  }

  @Override
  public void getAttributeAs(int index, TypedValueDecoder tvd) throws XMLStreamException
  {
    mXMLStreamReader.getAttributeAs(index, tvd);
  }

  @Override
  public byte[] getAttributeAsBinary(int index) throws XMLStreamException
  {
    return mXMLStreamReader.getAttributeAsBinary(index);
  }

  @Override
  public byte[] getAttributeAsBinary(int index, Base64Variant v) throws XMLStreamException
  {
    return mXMLStreamReader.getAttributeAsBinary(index, v);
  }

  @Override
  public int[] getAttributeAsIntArray(int index) throws XMLStreamException
  {
    return mXMLStreamReader.getAttributeAsIntArray(index);
  }

  @Override
  public long[] getAttributeAsLongArray(int index) throws XMLStreamException
  {
    return mXMLStreamReader.getAttributeAsLongArray(index);
  }

  @Override
  public float[] getAttributeAsFloatArray(int index) throws XMLStreamException
  {
    return mXMLStreamReader.getAttributeAsFloatArray(index);
  }

  @Override
  public double[] getAttributeAsDoubleArray(int index) throws XMLStreamException
  {
    return mXMLStreamReader.getAttributeAsDoubleArray(index);
  }

  @Override
  public int getAttributeAsArray(int index, TypedArrayDecoder tad) throws XMLStreamException
  {
    return mXMLStreamReader.getAttributeAsArray(index, tad);
  }

  @Override
  public Object getProperty(String name) throws IllegalArgumentException
  {
    return mXMLStreamReader.getProperty(name);
  }

  @Override
  public int next() throws XMLStreamException
  {
    return mXMLStreamReader.next();
  }

  @Override
  public void require(int type, String namespaceURI, String localName) throws XMLStreamException
  {
    mXMLStreamReader.require(type, namespaceURI, localName);
  }

  @Override
  public String getElementText() throws XMLStreamException
  {
    return mXMLStreamReader.getElementText();
  }

  @Override
  public int nextTag() throws XMLStreamException
  {
    return mXMLStreamReader.nextTag();
  }

  @Override
  public boolean hasNext() throws XMLStreamException
  {
    return mXMLStreamReader.hasNext();
  }

  @Override
  public void close() throws XMLStreamException
  {
    mXMLStreamReader.close();
  }

  @Override
  public String getNamespaceURI(String prefix)
  {
    return mXMLStreamReader.getNamespaceURI(prefix);
  }

  @Override
  public boolean isStartElement()
  {
    return mXMLStreamReader.isStartElement();
  }

  @Override
  public boolean isEndElement()
  {
    return mXMLStreamReader.isEndElement();
  }

  @Override
  public boolean isCharacters()
  {
    return mXMLStreamReader.isCharacters();
  }

  @Override
  public boolean isWhiteSpace()
  {
    return mXMLStreamReader.isWhiteSpace();
  }

  @Override
  public String getAttributeValue(String namespaceURI, String localName)
  {
    return mXMLStreamReader.getAttributeValue(namespaceURI, localName);
  }

  @Override
  public int getAttributeCount()
  {
    return mXMLStreamReader.getAttributeCount();
  }

  @Override
  public QName getAttributeName(int index)
  {
    return mXMLStreamReader.getAttributeName(index);
  }

  @Override
  public String getAttributeNamespace(int index)
  {
    return mXMLStreamReader.getAttributeNamespace(index);
  }

  @Override
  public String getAttributeLocalName(int index)
  {
    return mXMLStreamReader.getAttributeLocalName(index);
  }

  @Override
  public String getAttributePrefix(int index)
  {
    return mXMLStreamReader.getAttributePrefix(index);
  }

  @Override
  public String getAttributeType(int index)
  {
    return mXMLStreamReader.getAttributeType(index);
  }

  @Override
  public String getAttributeValue(int index)
  {
    return mXMLStreamReader.getAttributeValue(index);
  }

  @Override
  public boolean isAttributeSpecified(int index)
  {
    return mXMLStreamReader.isAttributeSpecified(index);
  }

  @Override
  public int getNamespaceCount()
  {
    return mXMLStreamReader.getNamespaceCount();
  }

  @Override
  public String getNamespacePrefix(int index)
  {
    return mXMLStreamReader.getNamespacePrefix(index);
  }

  @Override
  public String getNamespaceURI(int index)
  {
    return mXMLStreamReader.getNamespaceURI(index);
  }

  @Override
  public NamespaceContext getNamespaceContext()
  {
    return mXMLStreamReader.getNamespaceContext();
  }

  @Override
  public int getEventType()
  {
    return mXMLStreamReader.getEventType();
  }

  @Override
  public String getText()
  {
    return mXMLStreamReader.getText();
  }

  @Override
  public char[] getTextCharacters()
  {
    return mXMLStreamReader.getTextCharacters();
  }

  @Override
  public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException
  {
    return mXMLStreamReader.getTextCharacters(sourceStart, target, targetStart, length);
  }

  @Override
  public int getTextStart()
  {
    return mXMLStreamReader.getTextStart();
  }

  @Override
  public int getTextLength()
  {
    return mXMLStreamReader.getTextLength();
  }

  @Override
  public String getEncoding()
  {
    return mXMLStreamReader.getEncoding();
  }

  @Override
  public boolean hasText()
  {
    return mXMLStreamReader.hasText();
  }

  @Override
  public Location getLocation()
  {
    return mXMLStreamReader.getLocation();
  }

  @Override
  public QName getName()
  {
    return mXMLStreamReader.getName();
  }

  @Override
  public String getLocalName()
  {
    return mXMLStreamReader.getLocalName();
  }

  @Override
  public boolean hasName()
  {
    return mXMLStreamReader.hasName();
  }

  @Override
  public String getNamespaceURI()
  {
    return mXMLStreamReader.getNamespaceURI();
  }

  @Override
  public String getPrefix()
  {
    return mXMLStreamReader.getPrefix();
  }

  @Override
  public String getVersion()
  {
    return mXMLStreamReader.getVersion();
  }

  @Override
  public boolean isStandalone()
  {
    return mXMLStreamReader.isStandalone();
  }

  @Override
  public boolean standaloneSet()
  {
    return mXMLStreamReader.standaloneSet();
  }

  @Override
  public String getCharacterEncodingScheme()
  {
    return mXMLStreamReader.getCharacterEncodingScheme();
  }

  @Override
  public String getPITarget()
  {
    return mXMLStreamReader.getPITarget();
  }

  @Override
  public String getPIData()
  {
    return mXMLStreamReader.getPIData();
  }

  @Override
  public XMLValidator validateAgainst(XMLValidationSchema schema) throws XMLStreamException
  {
    return mXMLStreamReader.validateAgainst(schema);
  }

  @Override
  public XMLValidator stopValidatingAgainst(XMLValidationSchema schema) throws XMLStreamException
  {
    return mXMLStreamReader.stopValidatingAgainst(schema);
  }

  @Override
  public XMLValidator stopValidatingAgainst(XMLValidator validator) throws XMLStreamException
  {
    return mXMLStreamReader.stopValidatingAgainst(validator);
  }

  @Override
  public ValidationProblemHandler setValidationProblemHandler(ValidationProblemHandler h)
  {
    return mXMLStreamReader.setValidationProblemHandler(h);
  }
}
