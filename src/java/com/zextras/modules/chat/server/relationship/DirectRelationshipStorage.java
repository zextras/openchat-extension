package com.zextras.modules.chat.server.relationship;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zextras.modules.chat.server.address.SpecificAddress;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 Ram storage for direct relationships.
 loadInitialValues() is used during the bootstrap instead of upsertRelationship() which is MUCH heavier.
 The class has been heavily tuned towards read speed rather than write which is really slow copy-on-write.
 Currently upsertRelationship() is used only for adding new relationships at runtime, while change of group and
 nick are handled directly in the existing reference for optimization purposes.

 @see DirectRelationshipModifier
 @see DistributionListRelationshipModifier
 */
@Singleton
public class DirectRelationshipStorage
{
  private final ReentrantLock                                     mLock;
  private HashMap<Integer, HashMap<SpecificAddress,Relationship>> mMap;

  @Inject
  public DirectRelationshipStorage()
  {
    mMap = new HashMap<Integer, HashMap<SpecificAddress,Relationship>>();
    mLock = new ReentrantLock(true);
  }

  public void loadInitialValues(int userId, Relationship relationship)
  {
    HashMap<SpecificAddress,Relationship> buddyMap = mMap.get(userId);
    if( buddyMap == null )
    {
      buddyMap = new HashMap<SpecificAddress,Relationship>();
      mMap.put(userId, buddyMap);
    }
    buddyMap.put(relationship.getBuddyAddress(), relationship);
  }

  public void removeRelationship(int userId, SpecificAddress buddy)
  {
    mLock.lock();
    try
    {
      HashMap<SpecificAddress, Relationship> newUserMap = mMap.get(userId);
      if( newUserMap == null){
        return;
      }

      newUserMap = (HashMap<SpecificAddress, Relationship>)  newUserMap.clone();
      newUserMap.remove(buddy);

      HashMap<Integer, HashMap<SpecificAddress,Relationship>> newMap =
        (HashMap<Integer, HashMap<SpecificAddress, Relationship>>) mMap.clone();

      newMap.put(userId, newUserMap);
      mMap = newMap;
    }
    finally
    {
      mLock.unlock();
    }
  }

  public void upsertRelationship(int userId, Relationship relationship)
  {
    mLock.lock();
    try
    {
      HashMap<SpecificAddress, Relationship> newUserMap = buildUserMap(
        mMap.get(userId),
        relationship
      );

      HashMap<Integer, HashMap<SpecificAddress,Relationship>> newMap =
        (HashMap<Integer, HashMap<SpecificAddress, Relationship>>) mMap.clone();

      newMap.put(userId, newUserMap);
      mMap = newMap;
    }
    finally
    {
      mLock.unlock();
    }
  }

  private HashMap<SpecificAddress, Relationship> buildUserMap(HashMap<SpecificAddress, Relationship> relationshipMap, Relationship newRelationship)
  {
    HashMap<SpecificAddress, Relationship> map;

    if( relationshipMap == null )
    {
      map = new HashMap<SpecificAddress, Relationship>();
    }
    else
    {
      map = (HashMap<SpecificAddress, Relationship>) relationshipMap.clone();
    }
    map.put(newRelationship.getBuddyAddress(), newRelationship);
    return map;
  }

  public Collection<Relationship> get(int userId)
  {
    HashMap<SpecificAddress, Relationship> buddyMap = mMap.get(userId);
    if( buddyMap != null )
    {
      return buddyMap.values();
    }
    else
    {
      return Collections.emptyList();
    }
  }

  public Relationship get(int userId,SpecificAddress specificAddress)
  {
    HashMap<SpecificAddress, Relationship> buddyMap = mMap.get(userId);
    if( buddyMap != null )
    {
      return buddyMap.get(specificAddress);
    }
    else
    {
      return null;
    }
  }

  public Map<SpecificAddress, Relationship> getMap(int userId, SpecificAddress userAddress)
  {
    HashMap<SpecificAddress, Relationship> buddyMap = mMap.get(userId);
    if( buddyMap != null )
    {
      return buddyMap;
    }
    else
    {
      return Collections.emptyMap();
    }
  }

  public Relationship assertRelationship(int userId, SpecificAddress specificAddress)
  {
    Relationship rel = get(userId, specificAddress);
    if( rel == null )
    {
      throw new RuntimeException("missing expected relationship");
    }

    return rel;
  }
}
