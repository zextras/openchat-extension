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
import com.zextras.modules.chat.server.User;
import com.zextras.modules.chat.server.address.SpecificAddress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Singleton
public class UserIdentityMap<T extends User> implements UserIdentityMapReader {
  private ReentrantLock mLock = new ReentrantLock();
  private final HashMap<String, T> mUserIdentityMap;

  @Inject
  public UserIdentityMap() {
    mUserIdentityMap = new HashMap<String, T>();
  }

  public void addUser(T user) {
    mLock.lock();
    try {
      mUserIdentityMap.put(user.getAddress().toString(), user);
    } finally {
      mLock.unlock();
    }
  }

  @Override
  public T getUser(SpecificAddress address) {
    mLock.lock();
    T user = null;
    try {
      user = mUserIdentityMap.get(address.toString());
    } finally {
      mLock.unlock();
    }
    return user;
  }

  public void removeUser(SpecificAddress address) {
    mLock.lock();
    try {
      mUserIdentityMap.remove(address.toString());
    } finally {
      mLock.unlock();
    }
  }

  @Override
  public boolean hasUser(SpecificAddress address) {
    return mUserIdentityMap.containsKey(address.toString());
  }

  public void clear() {
    mUserIdentityMap.clear();
  }

  @Override
  public List<User> getAllUsers()
  {
    mLock.lock();
    try {
      return new ArrayList<User>(mUserIdentityMap.values());
    } finally {
      mLock.unlock();
    }
  }
}
