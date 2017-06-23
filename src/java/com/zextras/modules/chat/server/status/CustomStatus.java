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

package com.zextras.modules.chat.server.status;

import com.zextras.modules.chat.server.db.PersistentEntity;

public class CustomStatus extends AbstractStatus implements Status, PersistentEntity
{
  private final StatusId mId;
  private final StatusType mType;
  private final String mText;

  public CustomStatus(int id,
                      String text,
                      StatusType type)
  {
    mId = new StatusId(id);
    mType = type;
    mText = text;
  }

  public String getText() {
    return mText;
  }

  public StatusType getType() {
    return mType;
  }

  @Override
  public StatusId getId() {
    return mId;
  }

  @Override
  public boolean canBeStored() {
    return true;
  }

  @Override
  public int getEntityId()
  {
    return mId.id();
  }
}
