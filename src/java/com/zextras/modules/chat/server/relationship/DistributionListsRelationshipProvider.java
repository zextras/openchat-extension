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

package com.zextras.modules.chat.server.relationship;

import com.google.inject.Inject;
import com.zextras.modules.chat.server.address.SpecificAddress;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Class provides user's distribution-list-relationship's information.
 * Distribution-list-relationship is a relationship derive from a
 * distribution list (contains the user's address) where all members has to be
 * friend.
 */
public class DistributionListsRelationshipProvider extends RelationshipProviderAdapter
{
  private final DistributionListExtractor mDistributionListExtractor;

  @Inject
  public DistributionListsRelationshipProvider(
    DistributionListExtractor distributionListExtractor
  )
  {
    mDistributionListExtractor = distributionListExtractor;
  }

  @Override
  public Collection<Relationship> getUserRelationships(int userId, SpecificAddress userAddress)
  {
    return mDistributionListExtractor.getDistributionListsRelationships(userAddress);
  }

  @Override
  public Relationship getUserRelationshipByBuddyAddress(
    int userId,
    SpecificAddress userAddress,
    SpecificAddress buddyAddress
  )
  {
    if ( mDistributionListExtractor.areBuddies(userAddress, buddyAddress))
    {
      return mDistributionListExtractor.buildRelationship(
        buddyAddress
      );
    }

    return null;
  }

  @Nullable
  @Override
  public Relationship.RelationshipType userRelationshipType(int userId, SpecificAddress userAddress, SpecificAddress buddyAddress)
  {
    return mDistributionListExtractor.areBuddies(
      userAddress,
      buddyAddress
    ) ? Relationship.RelationshipType.ACCEPTED : null;
  }
}
