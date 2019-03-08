package com.zextras.modules.chat.server.listener;

import com.zextras.modules.chat.server.encoding.Encoder;
import com.zextras.modules.chat.server.events.EventInterpreter;
import org.openzal.zal.soap.SoapResponse;

public interface PingQueueListener extends EventQueueListener
{
  boolean alreadyReplied();
  void encodeEvents(SoapResponse response, EventInterpreter<Encoder> encoderFactory);
  void suspendContinuation(Object continuationObject, long timeoutInMs);
}
