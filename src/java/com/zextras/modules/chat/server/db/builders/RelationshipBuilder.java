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

package com.zextras.modules.chat.server.db.builders;

import com.zextras.modules.chat.server.Relationship;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.exceptions.ChatDbException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RelationshipBuilder implements Builder<Relationship>
{
  private ResultSet mRs;

  public RelationshipBuilder(ResultSet rs) {
    mRs = rs;
  }

  @Override
  public boolean next()
    throws ChatDbException
  {
    try {
      return mRs.next();
    } catch (SQLException e) {
      ChatDbException newEx = new ChatDbException(e.getMessage());
      newEx.initCause(e);
      throw newEx;
    }
  }

  public Relationship build()
    throws ChatDbException
  {
    try
    {
      int userId = mRs.getInt("USERID");
      SpecificAddress buddyAddress = new SpecificAddress(mRs.getString("BUDDYADDRESS"));
      Relationship.RelationshipType type = Relationship.RelationshipType.fromByte(mRs.getByte("TYPE"));
      String buddyNickname = mRs.getString("BUDDYNICKNAME");
      return new Relationship(userId, buddyAddress, type, buddyNickname, mRs
      .getString("GROUP"));
    }
    catch (SQLException e)
    {
      ChatDbException newEx = new ChatDbException(e.getMessage());
      newEx.initCause(e);
      throw newEx;
    }
  }
}
