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

import chipmunk.runtime.Fiber;
import chipmunk.vm.tree.Node;

public class FunctionNode implements Node {
    public Node[] nodes;

    public FunctionNode(){}

    public FunctionNode(Node... body){
        nodes = body;
    }

    @Override
    public Object execute(Fiber ctx) {
        for (int i = 0; i < nodes.length - 1; i++) {
            var v = nodes[i].execute(ctx);
            if (ctx._return) {
                return v;
            }
        }
        return nodes[nodes.length - 1].execute(ctx);
    }
}
