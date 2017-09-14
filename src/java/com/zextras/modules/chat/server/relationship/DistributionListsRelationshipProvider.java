package com.zextras.modules.chat.server.relationship;

import com.google.inject.Inject;
import com.zextras.lib.AccountHelper;
import com.zextras.lib.filters.FilteredIterator;
import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.properties.ChatProperties;
import com.zextras.modules.chat.server.Relationship;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.mappers.UserInfoMapper;
import com.zextras.modules.chat.server.db.providers.UserInfo;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.utils.UserDiscriminant;
import org.openzal.zal.Account;
import org.openzal.zal.DistributionList;
import org.openzal.zal.Provisioning;
import org.openzal.zal.Utils;
import org.openzal.zal.exceptions.NoSuchAccountException;
import org.openzal.zal.lib.Filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Class provides user's distribution-list-relationship's information.
 * Distribution-list-relationship is a relationship derive from a
 * distribution list (contains the user's address) where all members has to be
 * friend.
 */
public class DistributionListsRelationshipProvider
  extends AbstractRelationshipProvider
{
  private final Provisioning     mProvisioning;
  private final UserDiscriminant mUserDiscriminant;
  private       UserInfoMapper   mUserInfoMapper;
  private final ChatProperties mChatProperties;
  
  @Inject
  public DistributionListsRelationshipProvider(Provisioning provisioning,
                                               UserDiscriminant userDiscriminant,
                                               UserInfoMapper userInfoMapper,
                                               ChatProperties chatProperties)
  {
    mProvisioning = provisioning;
    mUserDiscriminant = userDiscriminant;
    mUserInfoMapper = userInfoMapper;
    mChatProperties = chatProperties;
  }
  
  @Override
  public Collection<Relationship> getUserRelationships(int userId)
  {
    Account account  = null;
    try
    {
      account = getUserAccountFromId(userId);
    }
    catch (ChatDbException e )
    {
      ChatLog.log.crit(Utils.exceptionToString(e));
      throw new RuntimeException(e);
    }
    catch (NoSuchAccountException ignore)
    {
      return Collections.emptyList();
    }
    return getDistributionListsRelationships(userId, account);
  }
  
  private Account assertAccountByAddress(String address)
  {
    return mProvisioning.assertAccountByName(address);
  }
  
  private Collection<Relationship> getDistributionListsRelationships(int userId,
                                                                     Account mUserAccount)
  {
    Collection<Relationship> distributionListsRelationships = new HashSet<Relationship>();
    Collection<String>       buddyGroupAddresses            = getBuddiesAddressesFromFriendDistributionLists(mUserAccount);
    for (String buddyAddress : buddyGroupAddresses)
    {
      SpecificAddress buddySpecificAddress = new SpecificAddress(buddyAddress);
      Relationship    buddyRelationship    =
        buildDefaultDistributionListsRelationship(userId, buddySpecificAddress);
      distributionListsRelationships.add(buddyRelationship);
    }
    return distributionListsRelationships;
  }
  
  private Collection<String> getBuddiesAddressesFromFriendDistributionLists(Account userAccount)
  {
    Collection<String> allDistributionListsBuddiesAddress = new HashSet<String>();
    
    final Iterator<DistributionList> distributionListsList =
      getFriendDistributionList(userAccount);
    while(distributionListsList.hasNext())
    {
      DistributionList friendDistributionList = distributionListsList.next();
      List<String> members = new ArrayList<String>(mProvisioning.getGroupMembers(friendDistributionList.getName()));
      Iterator<String> userIterator = members.iterator();
      
      while (userIterator.hasNext())
      {
        String userAddress = userIterator.next();
        if (mUserDiscriminant.isUser(userAddress))
        {
          allDistributionListsBuddiesAddress.add(userAddress);
        }
      }
    }
    allDistributionListsBuddiesAddress.removeAll(userAccount.getAllAddresses());
    return allDistributionListsBuddiesAddress;
  }
  
  private Iterator<DistributionList> getFriendDistributionList(final Account userAccount){
    Filter<DistributionList> filter = new Filter<DistributionList>()
    {
      @Override
      public boolean filterOut(final DistributionList itemInfo)
      {
        return !mChatProperties.chatAllowDlMemberAddAsFriend(itemInfo.getName());
      }
  
      @Override
      public void setChildFilter(final Filter<DistributionList> filter)
      {
    
      }
    };
    
    Collection<DistributionList> distributionLists = userAccount.getDistributionLists(false,
                                                                                      null);
    return new FilteredIterator<DistributionList>(filter, distributionLists
      .iterator());
  }
  
  private Relationship buildDefaultDistributionListsRelationship(int userId,
                                                                 SpecificAddress buddySpecificAddress)
  {
    Account buddyAccount     = mProvisioning.assertAccountByName(buddySpecificAddress.toString());
    AccountHelper accountHelper = new AccountHelper(
            buddySpecificAddress.toString(),
                                  mProvisioning);
    String  buddyAccountName = accountHelper.getName();
    return new Relationship(userId,
                            buddySpecificAddress,
                            Relationship.RelationshipType.ACCEPTED,
                            buddyAccountName,
                            "");
  }
  
  @Override
  public Relationship getUserRelationshipByBuddyAddress(int userId,
                                                        SpecificAddress buddyAddress)
  {
    Collection<String> distributionListBuddiesAddresses = null;
    try
    {
      distributionListBuddiesAddresses = getBuddiesAddressesFromFriendDistributionLists(userId);
    }
    catch (ChatDbException e)
    {
      ChatLog.log.crit(Utils.exceptionToString(e));
      throw new RuntimeException(e);
    }
    if (distributionListBuddiesAddresses.contains(buddyAddress.toString()))
    {
      return buildDefaultDistributionListsRelationship(userId,
                                                       buddyAddress);
    }
    return null;
  }
  
  private Collection<String> getBuddiesAddressesFromFriendDistributionLists(int userId)
    throws
    ChatDbException
  {
    Account account  = getUserAccountFromId(userId);
    return getBuddiesAddressesFromFriendDistributionLists(account);
  }
  
  private Account getUserAccountFromId(int userId)
    throws
    ChatDbException
  {
    UserInfo userInfo = mUserInfoMapper.get(userId);
    return assertAccountByAddress(userInfo.getAddress().toString());
  }
  
  
}
