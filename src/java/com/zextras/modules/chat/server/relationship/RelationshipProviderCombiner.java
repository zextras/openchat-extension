package com.zextras.modules.chat.server.relationship;

import com.google.inject.Inject;
import com.zextras.modules.chat.server.Relationship;
import com.zextras.modules.chat.server.address.SpecificAddress;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;

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
    Collection<Relationship> directRelationships = mDirectRelationshipProvider.getUserRelationships(
      userId,
      userAddress
    );
    Collection<Relationship> allRelationships = new HashSet<Relationship>(
      Math.max(distributionListsRelationships.size(),distributionListsRelationships.size())
    );
    allRelationships.addAll(distributionListsRelationships);

    for (Relationship relationship : directRelationships)
    {
      Relationship distributionListsRelationship = mDistributionListRelationshipProvider.getUserRelationshipByBuddyAddress(
        userId,
        userAddress, relationship.getBuddyAddress()
      );
      if (distributionListsRelationship != null)
      {
        /* if there is a conflict, user direct relationship's attributes has
        the priority, except the relationship type */
        allRelationships.remove(distributionListsRelationship);
        relationship.updateVolatileType(Relationship.RelationshipType.ACCEPTED);
      }
      allRelationships.add(relationship);
    }
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
