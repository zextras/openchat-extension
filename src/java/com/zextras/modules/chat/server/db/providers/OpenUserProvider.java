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

package com.zextras.modules.chat.server.db.providers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zextras.modules.chat.server.*;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.mappers.UserInfoIteratorMapper;
import com.zextras.modules.chat.server.db.mappers.UserInfoMapper;
import com.zextras.modules.chat.server.db.modifiers.UserModifier;
import com.zextras.modules.chat.server.events.EventQueue;
import com.zextras.modules.chat.server.events.EventQueueFactory;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.relationship.DirectRelationshipProvider;
import com.zextras.modules.chat.server.relationship.RelationshipModifier;
import com.zextras.modules.chat.server.relationship.RelationshipProvider;
import org.jetbrains.annotations.NotNull;
import org.openzal.zal.Account;
import org.openzal.zal.Provisioning;
import org.openzal.zal.Utils;
import org.openzal.zal.exceptions.ZimbraException;

import java.util.concurrent.locks.ReentrantLock;

@Singleton
public class OpenUserProvider implements UserProvider
{
  private final UserInfoMapper                mUserInfoMapper;
  private final UserIdentityMap<InternalUser> mUserIdentityMap;
  private final UserModifier                  mUserModifier;
  private final Provisioning                  mProvisioning;
  private final UserInfoIteratorMapper        mUserInfoIteratorMapper;
  private final RelationshipProvider   mRelationshipProvider;
  private final RelationshipModifier   mRelationshipModifier;
  private final DirectRelationshipProvider mDirectRelationshipProvider;
  private final EventQueueFactory mEventQueueFactory;
  private ReentrantLock mLock = new ReentrantLock();


  @Inject
  public OpenUserProvider(
    UserInfoMapper userInfoMapper,
    UserIdentityMapImpl userIdentityMap,
    UserModifier userModifier,
    Provisioning provisioning,
    UserInfoIteratorMapper userInfoIteratorMapper,
    RelationshipProvider relationshipProvider,
    RelationshipModifier relationshipModifier,
    final DirectRelationshipProvider directRelationshipProvider,
    EventQueueFactory eventQueueFactory
  )
  {
    mUserInfoMapper = userInfoMapper;
    mRelationshipProvider = relationshipProvider;
    mRelationshipModifier = relationshipModifier;
    mUserIdentityMap = userIdentityMap;
    mUserModifier = userModifier;
    mProvisioning = provisioning;
    mUserInfoIteratorMapper = userInfoIteratorMapper;
    mDirectRelationshipProvider = directRelationshipProvider;
    mEventQueueFactory = eventQueueFactory;
  }

  @Override
  public InternalUser getUser(SpecificAddress address)
          throws ChatDbException
  {
    return getUser(address,false);
  }

  @Override
  public InternalUser getUser(SpecificAddress address,boolean skipLocalHostCheck)
    throws ChatDbException
  {
    InternalUser userInstance = getUserFromCache(address);
    if (userInstance != null) {
      return userInstance;
    }

    Account account;
    try {
      account = mProvisioning.getAccountByName(address.toString());
      if( account == null || (!skipLocalHostCheck && !mProvisioning.onLocalServer(account)) )
      {
        throw new RuntimeException("Invalid request for account " + address.toString());
      }
    } catch (ZimbraException e) {
      throw new RuntimeException("Invalid request for account " + address.toString() +
                                   ": " + Utils.exceptionToString(e));
    }

    SpecificAddress accountName = new SpecificAddress(account.getName());
    mLock.lock();
    try {
      userInstance = getUserFromCache(accountName);
      if (userInstance != null) {
        return userInstance;
      }

      UserInfo userInfo = mUserInfoMapper.get(accountName);

      if (!userInfo.isValid()) {
        userInfo = new UserInfo(0, accountName);
        int userId = mUserModifier.insertUser(userInfo);
        userInfo = new UserInfo(userId, userInfo.getAddress());
      }

      EventQueue eventQueue = mEventQueueFactory.create(0);
      InternalUser user = buildUser(userInfo, eventQueue);

      mUserIdentityMap.addUser(user);

      return user;
    } finally {
      mLock.unlock();
    }
  }

  @NotNull
  private InternalUser buildUser(UserInfo userInfo, EventQueue eventQueue) throws ChatDbException
  {

    return new InternalUser(userInfo.getId(),
                            userInfo.getAddress(),
                            eventQueue,
                            mUserModifier,
                            mRelationshipProvider,
                            mRelationshipModifier,
                            mDirectRelationshipProvider);
  }

  @Override
  public void visitAllUsers(UserVisitor visitor) throws ChatDbException
  {
    visitAllUsers(mUserIdentityMap, visitor);
  }

  public void visitAllUsers(UserIdentityMap userIdentityMap, UserVisitor visitor) throws ChatDbException
  {
    for( UserInfo userInfo : mUserInfoIteratorMapper.get() )
    {
      User user;

      if( userIdentityMap.hasUser(userInfo.getAddress()) )
      {
        user = userIdentityMap.getUser(userInfo.getAddress());
      }
      else
      {
        user = buildUser(userInfo, mEventQueueFactory.create(0));
      }

      visitor.visitUser(user);
    }
  }

  @Override
  public InternalUser getUserFromCache(SpecificAddress address) {
    InternalUser userInstance = mUserIdentityMap.getUser(address);
    if (userInstance != null)
    {
      return userInstance;
    }
    return null;
  }

  // Used only for full clear of chat
  @Override
  public void clearCache() {
    mUserIdentityMap.clear();
  }

  @Override
  public boolean isLocal(SpecificAddress address)
  {
    try {
      Account account = mProvisioning.getAccountByName(address.toString());
      if( account != null && mProvisioning.onLocalServer(account) )
      {
        return true;
      }
    } catch (ZimbraException ignored) {}

    return false;
  }

  @Override
  public void clearCache(SpecificAddress address)
  {
    mUserIdentityMap.removeUser(address);
  }
}
