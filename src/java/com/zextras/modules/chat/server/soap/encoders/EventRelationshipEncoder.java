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

package com.zextras.modules.chat.server.soap.encoders;

import com.zextras.lib.json.JSONArray;
import com.zextras.lib.json.JSONObject;
import com.zextras.modules.chat.server.response.ChatSoapResponse;
import com.zextras.modules.chat.server.status.FixedStatus;
import com.zextras.modules.chat.server.status.PartialStatus;
import com.zextras.modules.chat.server.Relationship;
import com.zextras.modules.chat.server.status.Status;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.client_contstants.ClientEventType;
import com.zextras.modules.chat.server.events.EventGetRelationships;
import com.zextras.modules.chat.server.soap.SoapEncoder;

public class EventRelationshipEncoder implements SoapEncoder
{
  private final EventGetRelationships mEvent;

  public EventRelationshipEncoder(EventGetRelationships event)
  {
    mEvent = event;
  }

  public void encode(ChatSoapResponse response, SpecificAddress target)
  {
    final JSONArray relationships = new JSONArray();
    for (Relationship relationship : mEvent.getRelationships())
    {
      relationships.put(encodeRelationship(relationship));
    }

    final JSONObject message = new JSONObject();
    message.put("type", ClientEventType.BUDDY_LIST);
    message.put("buddy_list", relationships);
    message.put("timestampSent", System.currentTimeMillis());

    response.addResponse(message);
  }

  /**
   * Encodes the relationship data to return to all
   * requester's sessions
   *
   * @param relationship the relationship to encode
   * @return the JSONObject containing the encoded data
   */


  private JSONObject encodeRelationship(final Relationship relationship)
  {
    final SpecificAddress buddyAddress = relationship.getBuddyAddress();

    final JSONObject relationshipJSON = new JSONObject();
    relationshipJSON.put("friend", buddyAddress.toString());  //TODO not use
    // by client
/*
ignored by the client
    relationshipJSON.put("type", relationship.getType());
*/
    String nickname = relationship.getBuddyNickname();
    if( nickname.isEmpty() ) {
      nickname = buddyAddress.toString();
    }
    relationshipJSON.put("nickname", nickname);
    relationshipJSON.put("id", buddyAddress.toString());
    relationshipJSON.put("group", relationship.getGroup());

    putStatusInfoIntoJSON(relationshipJSON, relationship, new FixedStatus(Status.StatusType.OFFLINE));

    return relationshipJSON;
  }

  private void putStatusInfoIntoJSON(JSONObject relationshipJSON, Relationship relationship, PartialStatus partialStatus)
  {
    if( relationship.getType() == Relationship.RelationshipType.ACCEPTED )
    {
      relationshipJSON.put("status", partialStatus.getText());
      relationshipJSON.put("statusType", partialStatus.getType());
    }
    else
    {
      relationshipJSON.put("statusType", relationship.getType().toStatusType());
    }
  }



}
