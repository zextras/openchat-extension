package com.zextras.modules.chat.server.relationship;

import com.google.inject.Inject;
import com.zextras.modules.chat.server.address.SpecificAddress;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Class provides user's relationship's information, hiding the
 * relationships's fonts.
 */
public class RelationshipProviderCombiner extends RelationshipProviderAdapter
{
  private final DirectRelationshipProvider            mDirectRelationshipProvider;
  private final DistributionListsRelationshipProvider mDistributionListRelationshipProvider;

  @Inject
  public RelationshipProviderCombiner(
    DirectRelationshipProvider directRelationshipProvider,
    DistributionListsRelationshipProvider distributionListRelationshipProvider
  )
  {
    mDirectRelationshipProvider = directRelationshipProvider;
    mDistributionListRelationshipProvider = distributionListRelationshipProvider;
  }

  /**
   * If a relationship is in both DirectRelationship.getRelationships(userAddress) and
   * DistributionListRelationship.getRelationships(userAddress), the method
   * will include only the directRelationship with type set
   * Relationship.RelationshipType.ACCEPTED.
   */
  @Override
  public Collection<Relationship> getUserRelationships(int userId, SpecificAddress userAddress)
  {
    Collection<Relationship> distributionListsRelationships = mDistributionListRelationshipProvider.getUserRelationships(
      userId,
      userAddress
    );
    Map<SpecificAddress, Relationship> directRelationshipsMap = mDirectRelationshipProvider.getUserRelationshipsMap(
      userId
    );

    if( distributionListsRelationships.isEmpty() )
    {
      return directRelationshipsMap.values();
    }

    if( directRelationshipsMap.isEmpty() )
    {
      return distributionListsRelationships;
    }

    //copy to update the map directly without changing the source
    directRelationshipsMap = new HashMap<>(directRelationshipsMap);

    Collection<Relationship> allRelationships = new ArrayList<>(
      distributionListsRelationships.size()+distributionListsRelationships.size()
    );

    for (Relationship relationship : distributionListsRelationships)
    {
      Relationship directRelationship = directRelationshipsMap.remove(relationship.getBuddyAddress());

      if (directRelationship != null)
      {
        Relationship copiedDirectRelationship = directRelationship.copy();
        copiedDirectRelationship.updateVolatileType(Relationship.RelationshipType.ACCEPTED);
        allRelationships.add(copiedDirectRelationship);
      }
      else
      {
        allRelationships.add(relationship);
      }
    }
    allRelationships.addAll(directRelationshipsMap.values());

    return allRelationships;
  }

  /**
   * If a relationship is in both DirectRelationship.getRelationships(userAddress) and
   * DistributionListRelationship.getRelationships(userAddress), the method
   * will return the directRelationship with type set Relationship.RelationshipType.ACCEPTED
   */
  @Override
  public Relationship getUserRelationshipByBuddyAddress(
    int userId,
    SpecificAddress userAddress, SpecificAddress buddyAddress
  )
  {
    //target relationship is in both collections
    Relationship directRelationship = mDirectRelationshipProvider.getUserRelationshipByBuddyAddress(
      userId,
      userAddress,
      buddyAddress
    );
    Relationship distributionListsRelationship = mDistributionListRelationshipProvider.getUserRelationshipByBuddyAddress(
      userId,
      userAddress,
      buddyAddress
    );
    if (directRelationship == null)
    {
      return distributionListsRelationship;
    }
    if (distributionListsRelationship == null)
    {
      return directRelationship;
    }

    //avoid change in the original relationship
    Relationship mergedRelationship = directRelationship.copy();
    mergedRelationship.updateVolatileType(Relationship.RelationshipType.ACCEPTED);

    return mergedRelationship;
  }

  @Nullable
  @Override
  public Relationship.RelationshipType userRelationshipType(int userId, SpecificAddress userAddress, SpecificAddress buddyAddress)
  {
    Relationship.RelationshipType type = mDirectRelationshipProvider.userRelationshipType(userId, userAddress, buddyAddress);
    if( type != Relationship.RelationshipType.ACCEPTED )
    {
      if( mDistributionListRelationshipProvider.userRelationshipType(userId, userAddress, buddyAddress) != null ) {
        return Relationship.RelationshipType.ACCEPTED;
      }
    }
    return type;
  }
}
