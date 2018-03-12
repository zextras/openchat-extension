/*
 * Copyright (C) 2018 ZeXtras S.r.l.
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

package com.zextras.modules.chat.server.events;

import com.zextras.lib.Container;
import com.zextras.lib.ContainerImpl;
import com.zextras.lib.Optional;
import com.zextras.modules.chat.server.Target;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.exceptions.ChatException;

public class EventSharedFile extends Event
{
  private final SpecificAddress           mSender;
  private final Optional<SpecificAddress> mOriginalTarget;
  private final FileInfo                  mFileInfo;
  private final TargetType                mEventType;

  public EventSharedFile(
    EventId eventId,
    SpecificAddress sender,
    Optional<SpecificAddress> originalTarget,
    Target target,
    long timestamp,
    FileInfo fileInfo,
    TargetType eventType
  )
  {
    super(eventId, sender, target, timestamp);
    mSender = sender;
    mOriginalTarget = originalTarget;
    mFileInfo = fileInfo;
    mEventType = eventType;
  }

  public FileInfo getFileInfo()
  {
    return mFileInfo;
  }

  public Optional<SpecificAddress> getOriginalTarget()
  {
    return mOriginalTarget;
  }

  public SpecificAddress getSender()
  {
    return mSender;
  }

  public Container getExtraInfo()
  {
    Container container = new ContainerImpl();

    container.putString("owner_id", mFileInfo.getOwnerId());
    container.putString("share_id", mFileInfo.getShareId());
    container.putString("file_name", mFileInfo.getFilename());
    container.putLong("file_size", mFileInfo.getFilesize());
    container.putString("content_type", mFileInfo.getContentType());
    if (mOriginalTarget.hasValue())
    {
      container.putString("original_target", mOriginalTarget.getValue().resourceAddress());
    }

    return container;
  }

  @Override
  public <T> T interpret(EventInterpreter<T> interpreter) throws ChatException
  {
    return interpreter.interpret(this);
  }

  public TargetType getType()
  {
    return mEventType;
  }

  public Event createCopy(SpecificAddress sender, Target target, TargetType eventType, Optional<SpecificAddress> originalTarget)
  {
    return new EventSharedFile(
      EventId.randomUUID(),
      sender,
      originalTarget,
      target,
      getTimestamp(),
      mFileInfo,
      eventType
    );
  }
}
