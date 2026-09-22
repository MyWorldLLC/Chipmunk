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

public class CListIterator extends HostCObject implements GCCollectable {

    protected final CList list;
    private int index = 0;

    public CListIterator(CList list) {
        this.list = list;
    }

    public boolean hasNext(){
        return index < list.size();
    }

    public double next(){
        var r = list.get(index);
        index++;
        return r;
    }

    @Override
    public void gcVisit(GarbageCollector.GCCollection collection) {
        collection.visit(list.selfPtr());
    }
}
