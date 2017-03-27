package com.zextras.modules.chat.server.relationship;

import com.google.inject.Inject;
import com.zextras.modules.chat.server.Relationship;
import com.zextras.modules.chat.server.address.SpecificAddress;

import java.util.Collection;
import java.util.HashSet;

/**
 * Class provides user's relationship's information, hiding the
 * relationships's fonts.
 */
public class RelationshipProviderProxy
  extends AbstractRelationshipProvider
{
  private final DirectRelationshipProvider            mDirectRelationshipProvider;
  private final DistributionListsRelationshipProvider mDistributionListRelationshipProvider;
  
  @Inject
  public RelationshipProviderProxy(DirectRelationshipProvider directRelationshipProvider,
                                   DistributionListsRelationshipProvider distributionListRelationshipProvider)
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
  public Collection<Relationship> getUserRelationships(int userId)
  {
    final Collection<Relationship> distributionListsRelationships = mDistributionListRelationshipProvider.getUserRelationships(userId);
    final Collection<Relationship> directRelationships            = mDirectRelationshipProvider.getUserRelationships(userId);
    final Collection<Relationship> allRelationships               = new HashSet<Relationship>(distributionListsRelationships.size());
    allRelationships.addAll(distributionListsRelationships);
    
    for (Relationship relationship : directRelationships)
    {
      Relationship distributionListsRelationship = mDistributionListRelationshipProvider.getUserRelationshipByBuddyAddress(userId,
                                                                                                                           relationship.getBuddyAddress());
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
  public Relationship getUserRelationshipByBuddyAddress(int userId,
                                                        SpecificAddress buddyAddress)
  {
    final Relationship directRelationship = mDirectRelationshipProvider.getUserRelationshipByBuddyAddress(userId,
                                                                                                          buddyAddress);
    final Relationship distributionListsRelationship = mDistributionListRelationshipProvider.getUserRelationshipByBuddyAddress(userId,
                                                                                                                               buddyAddress);
    if (directRelationship == null)
    {
      return distributionListsRelationship;
    }
    if (distributionListsRelationship == null)
    {
      return directRelationship;
    }
    //target relationship is in both collections
    directRelationship.updateVolatileType(Relationship.RelationshipType.ACCEPTED);
    return directRelationship;
  }
}
