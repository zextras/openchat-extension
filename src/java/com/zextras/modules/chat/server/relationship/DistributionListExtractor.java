package com.zextras.modules.chat.server.relationship;

import com.google.inject.Singleton;
import com.zextras.lib.AccountHelper;
import com.zextras.lib.filters.FilteredIterator;
import com.zextras.modules.chat.properties.ChatProperties;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.utils.UserDiscriminant;
import org.openzal.zal.Account;
import org.openzal.zal.DistributionList;
import org.openzal.zal.Provisioning;
import org.openzal.zal.exceptions.NoSuchAccountException;
import org.openzal.zal.lib.Filter;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

@Singleton
public class DistributionListExtractor
{
  private final Provisioning             mProvisioning;
  private final UserDiscriminant         mUserDiscriminant;
  private       Filter<DistributionList> mAllowDlMembersFilter;

  @Inject
  public DistributionListExtractor(
    Provisioning provisioning,
    UserDiscriminant userDiscriminant,
    ChatProperties chatProperties
  )
  {
    mProvisioning = provisioning;
    mUserDiscriminant = userDiscriminant;
    mAllowDlMembersFilter = new DistributionListFilter(chatProperties);
  }

  public Collection<Relationship> getDistributionListsRelationships(
    SpecificAddress userAddress
  )
  {
    Account userAccount;
    try
    {
      userAccount = mProvisioning.assertAccountByName(userAddress.withoutResource().toString());
    }
    catch (NoSuchAccountException ignore)
    {
      return Collections.emptyList();
    }

    Collection<Relationship> distributionListsRelationships = new HashSet<Relationship>();

    Collection<SpecificAddress> buddyGroupAddresses = getBuddiesAddresses(
      userAccount, getFriendDistributionList(userAccount)
    );
    for (SpecificAddress buddySpecificAddress : buddyGroupAddresses)
    {
      distributionListsRelationships.add(
        buildRelationship(
          buddySpecificAddress
        )
      );
    }
    return distributionListsRelationships;
  }

  private Collection<SpecificAddress> getBuddiesAddresses(
    Account userAccount,
    Iterator<DistributionList> distributionListsIterator
  )
  {
    Collection<SpecificAddress> allDistributionListsBuddiesAddress = new HashSet<SpecificAddress>();
    List<String> userAddresses = userAccount.getAllAddresses();

    while (distributionListsIterator.hasNext())
    {
      DistributionList friendDistributionList = distributionListsIterator.next();
      Collection<String> members = mProvisioning.getGroupMembers(
        friendDistributionList.getName()
      );

      for (String memberAddress : members)
      {
        if (mUserDiscriminant.isUser(memberAddress) && !userAddresses.contains(memberAddress))
        {
          allDistributionListsBuddiesAddress.add(
            new SpecificAddress(memberAddress).withoutResource().intern()
          );
        }
      }
    }
    return allDistributionListsBuddiesAddress;
  }

  public boolean areBuddies( SpecificAddress buddy1, SpecificAddress buddy2 )
  {
    Account account1 = mProvisioning.getAccountByName(buddy1.toString());
    Account account2 = mProvisioning.getAccountByName(buddy2.toString());
    if( account1 == null || account2 == null ) {
      return false;
    }

    Iterator<DistributionList> it1 = getFriendDistributionList(account1);
    Iterator<DistributionList> it2 = getFriendDistributionList(account2);
    if( !it1.hasNext() || !it2.hasNext() ) {
      return false;
    }

    HashSet<String> distributionLists2 = toHash( it2 );
    while( it1.hasNext() )
    {
      String id = it1.next().getId();
      if (distributionLists2.contains(id))
      {
        return true;
      }
    }
    return false;
  }

  private HashSet<String> toHash(Iterator<DistributionList> it)
  {
    HashSet<String> set = new HashSet<String>();
    while( it.hasNext() )
    {
      DistributionList dl = it.next();
      set.add(dl.getId());
    }
    return set;
  }

  private Iterator<DistributionList> getFriendDistributionList(Account userAccount)
  {
    Collection<DistributionList> distributionLists = userAccount.getDistributionLists(
      false,
      null
    );
    return new FilteredIterator<DistributionList>(
      mAllowDlMembersFilter, distributionLists.iterator()
    );
  }

  public Relationship buildRelationship(
    SpecificAddress buddySpecificAddress
  )
  {
    AccountHelper buddyAccountHelper = new AccountHelper(
      buddySpecificAddress.withoutResource().toString(),
      mProvisioning
    );
    return new Relationship(
      buddySpecificAddress,
      Relationship.RelationshipType.ACCEPTED,
      buddyAccountHelper.getName(),
      ""
    );
  }

  private static class DistributionListFilter implements Filter<DistributionList>
  {
    private final ChatProperties mChatProperties;

    DistributionListFilter(ChatProperties chatProperties) {mChatProperties = chatProperties;}

    @Override
    public boolean filterOut(final DistributionList distributionList)
    {
      return !mChatProperties.chatAllowDlMemberAddAsFriend(distributionList);
    }

    @Override
    public void setChildFilter(final Filter<DistributionList> filter)
    {
      throw new RuntimeException();
    }
  }
}
