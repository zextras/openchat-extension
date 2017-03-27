/*
 * ZAL - The abstraction layer for Zimbra.
 * Copyright (C) 2017 ZeXtras S.r.l.
 *
 * This file is part of ZAL.
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
 * You should have received a copy of the GNU General Public License
 * along with ZAL. If not, see <http://www.gnu.org/licenses/>.
 */

package com.zextras.modules.chat.server;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.zextras.utils.Jsonable;
import com.zextras.utils.ToJSONSerializer;

import java.util.HashMap;
import java.util.Map;

@JsonSerialize(using = ToJSONSerializer.class)
public enum WritingState implements Jsonable
{
  RESET(-1),  //user stopped writing
  STOPPED(0), //user stopped writing
  WRITING(1), //user is writing
  GONE(2)     //user has closed the window
  ;

  private static final Map<Byte, WritingState> mByteToType = new HashMap<Byte, WritingState>();

  static
  {
    for (WritingState type : WritingState.values())
    {
      mByteToType.put(type.toByte(), type);
    }
  }

  private final byte mTypeByte;
  WritingState(int typeByte) {
    mTypeByte = (byte)typeByte;
  }

  public byte toByte() {
    return mTypeByte;
  }

  public Byte toJSON()
  {
    return toByte();
  }

  public static WritingState fromByte(byte value) {
    WritingState type = mByteToType.get(value);
    return type != null ? type : RESET;
  }
}
