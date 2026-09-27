/*
 * Copyright (C) 2026 MyWorld, LLC
 * All rights reserved.
 *
 * This file is part of Chipmunk.
 *
 * Chipmunk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Chipmunk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Chipmunk.  If not, see <https://www.gnu.org/licenses/>.
 */

package chipmunk.runtime;

import chipmunk.vm.hazel.GCCollectable;
import chipmunk.vm.hazel.GarbageCollector;

public class CListRange extends HostCObject implements GCCollectable {

    protected final CList list;
    protected final CRange range;

    public CListRange(CList list, CRange range) {
        this.list = list;
        this.range = range;
    }

    public CListRangeIterator iterator(){
        return new CListRangeIterator(list, range);
    }

    @Override
    public void gcVisit(GarbageCollector.GCCollection collection) {
        collection.visit(list.selfPtr());
    }

}
