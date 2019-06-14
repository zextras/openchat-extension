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

package com.zextras.modules.chat.server.destinations;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zextras.lib.FixedCacheMap;
import com.zextras.lib.FixedCacheStringTTLMap;
import com.zextras.lib.SoapClientHelper;
import com.zextras.lib.json.JSONException;
import com.zextras.lib.json.JSONObject;
import com.zextras.lib.log.ChatLog;
import com.zextras.lib.switches.Service;
import com.zextras.modules.chat.server.DestinationQueue;
import com.zextras.modules.chat.server.Priority;
import com.zextras.modules.chat.server.address.AddressResolver;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventDestination;
import com.zextras.modules.chat.server.events.EventDestinationProvider;
import com.zextras.modules.chat.server.events.EventIQQuery;
import com.zextras.modules.chat.server.events.EventInterpreterAdapter;
import com.zextras.modules.chat.server.events.EventLastMessageInfo;
import com.zextras.modules.chat.server.events.EventMessageHistory;
import com.zextras.modules.chat.server.events.EventMessageHistoryLast;
import com.zextras.modules.chat.server.events.EventRouter;
import com.zextras.modules.chat.server.events.EventSharedFile;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.core.ProvisioningCache;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.openzal.zal.Account;
import org.openzal.zal.OperationContext;
import org.openzal.zal.Provisioning;
import org.openzal.zal.Server;
import org.openzal.zal.Utils;
import org.openzal.zal.XMLElement;
import org.openzal.zal.lib.Clock;
import org.openzal.zal.lib.Version;
import org.openzal.zal.soap.SoapElement;
import org.openzal.zal.soap.SoapTransport;

@Singleton
public class LocalServerDestination implements EventDestination, EventDestinationProvider, Service
{
  public static final int DEFAULT_LOCAL_XMPP_PORT = 5269;
  private final Priority mPriority = new Priority(2);

  private final Provisioning                  mProvisioning;
  private final Map<String, DestinationQueue> mDestinationQueues;
  private final DestinationQueueFactory       mDestinationQueueFactory;
  private final AddressResolver               mAddressResolver;
  private final EventRouter                   mEventRouter;
  private final FilterNoMatchChatVersion      mFilterNoMatchChatVersion;

  @Inject
  public LocalServerDestination(
    ProvisioningCache provisioning,
    EventRouter eventRouter,
    DestinationQueueFactory destinationQueueFactory,
    AddressResolver addressResolver,
    SoapClientHelper soapClientHelper,
    Clock clock
  )
  {
    mProvisioning = provisioning;
    mDestinationQueueFactory = destinationQueueFactory;
    mAddressResolver = addressResolver;
    mDestinationQueues = new HashMap<String, DestinationQueue>();
    mEventRouter = eventRouter;
    mFilterNoMatchChatVersion = new FilterNoMatchChatVersion(provisioning,clock,soapClientHelper);
  }

  @Override
  public boolean deliverEvent(Event event, SpecificAddress address)
  {
    ChatLog.log.debug("LocalServerDestination: deliverEvent: "+event.getClass().getName()+" to "+address.resourceAddress());

    try
    {
      String host;

      boolean isServerAddress = address.getDomain().isEmpty();
      if( isServerAddress )
      {
        host = address.toString();
      }
      else
      {
        Account account = mProvisioning.getAccountByName(address.toString());
        if (account == null)
        {
          host = mAddressResolver.tryResolveAddress(address);
        }
        else
        {
          host = account.getMailHost();
        }
      }

      if( host == null ||
          host.equals(mProvisioning.getLocalServer().getServerHostname()) ||
          mFilterNoMatchChatVersion.isFiltered(host,event))
      {
        return false;
      }

      DestinationQueue destinationQueue;
      if (mDestinationQueues.containsKey(host))
      {
        destinationQueue = mDestinationQueues.get(host);
      }
      else
      {
        destinationQueue = mDestinationQueueFactory.createQueue(host);
        destinationQueue.start();
        mDestinationQueues.put(host, destinationQueue);
      }
      destinationQueue.addEvent(event, address);
      return true;
    }
    catch (Exception ex)
    {
      ChatLog.log.warn("unable to relay message: " + Utils.exceptionToString(ex));
      ChatLog.log.debug("event: " + event.getClass().getName());
      ChatLog.log.debug(Utils.exceptionToString(ex));
      return true;
    }
  }

  @Override
  public Collection<? extends EventDestination> getDestinations(SpecificAddress address)
  {
    return Collections.singletonList(this);
  }

  @Override
  public Priority getPriority() {
    return mPriority;
  }

  public void stop() {
    mEventRouter.unplugDestinationProvider(this);
    Set<String> queues = mDestinationQueues.keySet();
    for (String queue : queues) {
      try {
        mDestinationQueues.get(queue).stop();
      } catch (InterruptedException e) {
        ChatLog.log.err(e.getMessage());
      }
    }
    mFilterNoMatchChatVersion.disconnectAll();
  }

  public void start()
  {
    mEventRouter.plugDestinationProvider(this);
    Set<String> queues = mDestinationQueues.keySet();
    for (String queue : queues) {
      mDestinationQueues.get(queue).start();
    }
  }

  private class FilterNoMatchChatVersion extends EventInterpreterAdapter<Boolean>
  {
    private final static int                             sSIZE = 10;
    private final static long                            sTTL  = 15L * 60L * 1000L;
    private final        Provisioning                    mProvisioning;
    private final        SoapClientHelper                mSoapClientHelper;
    private final        FixedCacheStringTTLMap<Version> mChatVersions;

    public FilterNoMatchChatVersion(
      Provisioning provisioning,
      Clock clock,
      SoapClientHelper soapClientHelper
    )
    {
      super(false);
      mProvisioning = provisioning;
      mSoapClientHelper = soapClientHelper;
      mChatVersions = new FixedCacheStringTTLMap<>(
        sSIZE,
        new FixedCacheMap.Getter<String, Version>() {
          @Override
          public Version get(String host)
          {
            OperationContext context = new OperationContext(mProvisioning.getZimbraUser(),true);
            Server server = mProvisioning.getServerByName(host);
            if (server != null)
            {
              SoapTransport soapTransport = mSoapClientHelper.openAdmin(context, server);
              XMLElement request = new XMLElement(""
                + "<zextras xmlns=\"urn:zimbraAdmin\">"
                + "  <module>ZxChat</module>"
                + "  <action>getServerStatus</action>"
                + "</zextras>");
              try
              {
                SoapElement response = soapTransport.invokeWithoutSession(request);
                String content = response.getAttribute("content");
                JSONObject jsonObject = JSONObject.fromString(content);
                return new Version(jsonObject.getJSONObject("response").getString("chat_server_version"));
              }
              catch( IOException | JSONException e )
              {
                ChatLog.log.err(Utils.exceptionToString(e));
              }
            }
            return null;
          }
        },
        clock,
        sTTL,
        true
      );
    }

    public void disconnectAll()
    {
      mChatVersions.clear();
    }

    public boolean isFiltered(String host,Event event) throws ChatException
    {
      Boolean filtered = event.interpret(this);
      if (filtered)
      {
        Version remoteChatVersion = mChatVersions.get(host);
        return !remoteChatVersion.isAtLeast(2, 3);
      }
      return false;
    }

    @Override
    public Boolean interpret(EventIQQuery event) throws ChatException
    {
      return true;
    }

    @Override
    public Boolean interpret(EventMessageHistory event) throws ChatException
    {
      return true;
    }

    @Override
    public Boolean interpret(EventMessageHistoryLast event) throws ChatException
    {
      return true;
    }

    @Override
    public Boolean interpret(EventSharedFile event) throws ChatException
    {
      return true;
    }

    @Override
    public Boolean interpret(EventLastMessageInfo event) throws ChatException
    {
      return true;
    }
  }
}
