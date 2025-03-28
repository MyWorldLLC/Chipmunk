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
import chipmunk.runtime.Suspend;
import chipmunk.vm.tree.Node;

import java.util.Arrays;

public class Block implements Node {
    protected final Node[] body;

    public Block(Node... body){
        this.body = body;
        System.out.println(Arrays.toString(body));
    }

    @Override
    public Object execute(Fiber ctx) {
        Object result = null;
        for (int i = 0; i < body.length && !ctx.checkInterrupt(); i++) {
            try {
                // TODO - suspension
                result = body[i].execute(ctx);
            } catch (Suspend e) {
                throw e;
            }
        }
        return result;
    }

    @Override
    public void debug(DebugPrinter debug){
        debug.enterNode("block");
        for(var node : body){
            node.debug(debug);
        }
        debug.exitNode();
    }
}
