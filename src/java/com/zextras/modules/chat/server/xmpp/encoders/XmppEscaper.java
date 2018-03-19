package com.zextras.modules.chat.server.xmpp.encoders;

import org.codehaus.stax2.io.EscapingWriterFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class XmppEscaper implements EscapingWriterFactory
{
  @Override
  public Writer createEscapingWriterFor(Writer w, String enc) throws UnsupportedEncodingException
  {
    return new JsonValueWriter(w);
  }

  @Override
  public Writer createEscapingWriterFor(OutputStream out, String enc) throws UnsupportedEncodingException {
    return new JsonValueWriter(new OutputStreamWriter(out, enc));
  }

  static class JsonValueWriter extends Writer {
    protected final Writer _out;

    public JsonValueWriter(Writer out) {
      _out = out;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException
    {
      for (int i = off, end = off+len; i < end; ++i) {
        write(cbuf[i]);
      }
    }

    @Override
    public void write(int ch) throws IOException
    {
      switch (ch) {
        case '<':  _out.write("&lt;");   break;
        case '>':  _out.write("&gt;");   break;
        case '&':  _out.write("&amp;");  break;
        case '\"': _out.write("&quot;"); break;
        case '\'': _out.write("&#039;"); break;
        default:
          _out.write(ch);
      }
    }

    @Override
    public void flush() throws IOException {
      _out.flush();
    }

    @Override
    public void close() throws IOException {
      _out.close();
    }
  }
}

