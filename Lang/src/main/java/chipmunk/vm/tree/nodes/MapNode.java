/*
 * Copyright (C) 2025 MyWorld, LLC
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

package chipmunk.vm.tree.nodes;

import chipmunk.runtime.CMap;
import chipmunk.runtime.Fiber;
import chipmunk.vm.tree.Node;

public class MapNode implements Node {

    protected final Node[] elements;

    public MapNode(Node... elements){
        this.elements = elements;
        if(elements.length % 2 != 0){
            throw new IllegalArgumentException("Element array must contain key/value pairs.");
        }
    }

    @Override
    public Object execute(Fiber ctx) {
        // TODO - suspension & memory tracing
        var map = new CMap(elements.length);

        for(int i = 0; i < elements.length; i += 2){
            var key = elements[i].execute(ctx);
            var value = elements[i + 1].execute(ctx);
            map.setAt(key, value);
        }

        return map;
    }
}
