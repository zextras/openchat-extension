package com.zextras.modules.chat.server.destinations;

import com.zextras.lib.log.ChatLog;
import com.zextras.lib.switches.Service;
import com.zextras.modules.chat.server.Priority;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.events.EventDestination;
import com.zextras.modules.chat.server.events.EventDestinationProvider;
import com.zextras.modules.chat.server.events.EventInterpreterAdapter;
import com.zextras.modules.chat.server.events.EventRouter;
import com.zextras.modules.chat.server.events.EventXmppDiscovery;
import com.zextras.modules.chat.server.exceptions.ChatException;
import com.zextras.modules.chat.server.exceptions.UnavailableResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openzal.zal.Utils;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Keep track of which group/channel/space is stored where by intercepting disco broadcasts/response and user invites.
 * The tracking is stored in a temporary map since it'll be repopulated once the user is back online.
 *
 * @see EventXmppDiscovery
 */
public class PassiveRoomResolverDestination implements EventDestination, EventDestinationProvider, Service
{
  private final EventRouter                                          mEventRouter;
  private final Interpreter                                          mInterpreter;
  private final AtomicReference<Map<SpecificAddress, String>> mMap;

  @Inject
  public PassiveRoomResolverDestination(
    EventRouter eventRouter
  )
  {
    mEventRouter = eventRouter;
    mInterpreter = new Interpreter();
    mMap = new AtomicReference(new HashMap());
  }

  private class Interpreter extends EventInterpreterAdapter<Void>
  {
    Interpreter()
    {
      super(null);
    }

    @Override
    public Void interpret(EventXmppDiscovery discovery) throws ChatException
    {
      if(discovery.getDiscoveryQuery() == EventXmppDiscovery.DiscoveryQuery.items && discovery.isResult() )
      {
        for( EventXmppDiscovery.Result result : discovery.getResults() )
        {
          updateInternalMap(
            result.getAddress(),
            result.getName(),
            discovery.getSender().toString()
          );
        }
      }
      return null;
    }
  }

  private void updateInternalMap(SpecificAddress address, String name, String sourceServer)
  {
    while( true )
    {
      Map<SpecificAddress, String> currentMap = mMap.get();
      String serverFound = currentMap.get(address);
      if (sourceServer.equalsIgnoreCase(serverFound)) {
        return;
      }

      Map<SpecificAddress, String> newMap = new HashMap<>(currentMap);
      newMap.put(address, sourceServer);

      boolean written = mMap.compareAndSet(currentMap, newMap);
      if( written ) return;
    }
  }

  /**
   * @param address of one room
   * @return the server address
   * @throws UnavailableResource when the address cannot be resolved
   */
  @NotNull
  public String resolveRoomAddress(SpecificAddress address) throws UnavailableResource
  {
    String server = mMap.get().get(address);
    if( server == null)
    {
      throw new UnavailableResource(address.toString());
    }

    return server;
  }

  /**
   * @param address of one room
   * @return the server address or null if non is found
   */
  @Nullable
  public String tryResolveRoomAddress(SpecificAddress address)
  {
    return mMap.get().get(address);
  }

  @Override
  public void deliverEvent(Event event, SpecificAddress address)
  {
    try
    {
      event.interpret(mInterpreter);
    }
    catch (ChatException e)
    {
      ChatLog.log.warn(Utils.exceptionToString(e));
    }
  }

  @Override
  public boolean canHandle(SpecificAddress address)
  {
    return true;
  }

  @Override
  public Collection<? extends EventDestination> getDestinations(SpecificAddress address)
  {
    return Collections.singleton(this);
  }

  @Override
  public void start() throws ServiceStartException
  {
    mEventRouter.plugDestinationProvider(this);
  }

  @Override
  public void stop()
  {
    mEventRouter.unplugDestinationProvider(this);
  }

  @Override
  public Priority getPriority()
  {
    return Priority.HIGHEST_PRIORITY;
  }
}
