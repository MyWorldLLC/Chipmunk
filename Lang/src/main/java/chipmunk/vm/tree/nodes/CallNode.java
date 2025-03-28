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

public class CallNode implements Node {
    public final int locals;
    public Node f;
    public Node[] args;

    public CallNode(int locals, Node f, Node... args) {
        this.locals = locals;
        this.f = f;
        this.args = args;
    }

    @Override
    public Object execute(Fiber ctx) {
        for (int i = 0; i < args.length; i++) {
            ctx.setLocal(i + locals, args[i].execute(ctx));
        }
        ctx.preCall(locals);
        var result = f.execute(ctx);
        ctx.postCall();
        return result;
    }
}
