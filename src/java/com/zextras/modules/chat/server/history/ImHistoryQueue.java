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

package com.zextras.modules.chat.server.history;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zextras.lib.log.ChatLog;
import com.zextras.lib.switches.Service;
import com.zextras.modules.chat.server.ChatMessage;
import org.openzal.zal.Account;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Singleton
public class ImHistoryQueue implements Service
{
  private final ImHistoryQueueHandlerFactory mImHistoryQueueHandlerFactory;
  private final Condition                    mEmptyQueue;
  private       ReentrantLock                mLock;
  private       List<ImHistoryItem>          mMessageQueue;
  private       ImHistoryQueueHandler        mImHistoryQueueHandler;

  @Inject
  public ImHistoryQueue(ImHistoryQueueHandlerFactory imHistoryQueueHandlerFactory)
  {
    mImHistoryQueueHandlerFactory = imHistoryQueueHandlerFactory;
    mLock = new ReentrantLock();
    mEmptyQueue = mLock.newCondition();
    mMessageQueue = new LinkedList<ImHistoryItem>();
  }

  public void addMessage(ChatMessage chat, Account account)
  {
    mLock.lock();
    try
    {
      ChatLog.log.info("Adding instant message to history queue: " +
                        "[" + chat.getSenderName() + "] -> [" + chat.getTargetName() + "]");
      mMessageQueue.add(new ImHistoryItem(chat, account));
      mEmptyQueue.signal();
    }
    finally {
      mLock.unlock();
    }
  }

  public List<ImHistoryItem> getMessages() throws InterruptedException
  {
    List<ImHistoryItem> queue = new LinkedList<ImHistoryItem>();

    mLock.lock();
    try
    {
      while (mMessageQueue.size() < 1)
      {
        mEmptyQueue.await();
      }
      queue.addAll(mMessageQueue);
      mMessageQueue.clear();
    }
    finally
    {
      mLock.unlock();
    }
    Thread.sleep(250L);

    mLock.lock();
    try
    {
      if (mMessageQueue.size() > 0)
      {
        queue.addAll(mMessageQueue);
        mMessageQueue.clear();
      }
    }
    finally
    {
      mLock.unlock();
    }

    return queue;
  }

  @Override
  public void start() throws ServiceStartException
  {
    mImHistoryQueueHandler = mImHistoryQueueHandlerFactory.create(this);
    mImHistoryQueueHandler.start();
  }

  @Override
  public void stop()
  {
    mLock.lock();
    try
    {
      mMessageQueue.clear();
    }
    finally
    {
      mLock.unlock();
    }
    mImHistoryQueueHandler.stop();
  }
}
