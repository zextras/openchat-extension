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

package com.zextras.modules.chat.server.events;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class EventLinkedList extends LinkedList<Event> {
  public void sort() {
    Collections.sort(this, new SortByTimestamp());
  }

  private class SortByTimestamp implements Comparator<Event> {
    @Override
    public int compare(Event o1, Event o2) {
      return o1.getTimestamp() < o2.getTimestamp() ? -1 : o1.getTimestamp() == o2.getTimestamp() ? 0 : 1;
    }
  }
}
