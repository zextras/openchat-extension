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

package com.zextras.modules.chat.server;

import com.zextras.modules.chat.server.encoding.Encoder;
import com.zextras.modules.chat.server.events.EventInterpreter;
import org.openzal.zal.lib.Clock;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.encoding.EncoderFactory;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.xmpp.encoders.XmppEncoder;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

public class QueuedEvent
{
  private final Event                     mEvent;
  private final EventInterpreter<Encoder> mEncoderFactory;
  private       long                      mLastRetry;
  private       long                      mRetry;
  private final SpecificAddress           mRecipient;
  private final Clock                     mClock;

  public QueuedEvent(Event event, SpecificAddress recipient, EventInterpreter<Encoder> encoderFactory, Clock clock)
  {
    mClock = clock;
    mLastRetry = 0;
    mRetry = 0;
    mEvent = event;
    mRecipient = recipient;
    mEncoderFactory = encoderFactory;
  }

  public Event getEvent()
  {
    return mEvent;
  }

  public long getNextRetry()
  {
    return mLastRetry + (1000L * Math.min(15, mRetry));
  }

  public long getRetryCount()
  {
    return mRetry;
  }

  public SpecificAddress getRecipient() {
    return mRecipient;
  }

  private void updateLastRetry()
  {
    mLastRetry = mClock.getCurrentTime().getTimeInMillis();
  }

  private void updateRetryCount()
  {
    mRetry++;
  }

  public void updateRetry()
  {
    updateLastRetry();
    updateRetryCount();
  }

  public String encodeToXmpp() throws XMLStreamException
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream(256);
    XmppEncoder encoder = (XmppEncoder)mEvent.interpret(mEncoderFactory);
    encoder.encode(out, mRecipient);
    String stanza;
    try
    {
      stanza = new String( out.toByteArray(), "UTF-8" );
    }
    catch (UnsupportedEncodingException e)
    {
      throw new RuntimeException("Unsupported encoding UTF??");
    }

    return stanza;
  }

}
