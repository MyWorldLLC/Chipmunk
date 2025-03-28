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

import static chipmunk.vm.tree.Conversions.toBoolean;

public class While implements Node {
    public Node test;
    public Node body;

    @Override
    public Object execute(Fiber ctx) {
        return doBody(ctx, doTest(ctx));
    }

    public boolean doTest(Fiber ctx) {
        try {
            return (Boolean) test.execute(ctx);
        } catch (Exception e) {
            ctx.suspendStateless(e, (c, s) -> doBody(ctx, toBoolean(s)));
        }
        return false;
    }

    public Object doBody(Fiber ctx, boolean t) {
        while (t && !ctx.checkInterrupt()) {
            try {
                body.execute(ctx);
            } catch (Exception e) {
                ctx.suspendStateless(e, this::doTest);
            }

            t = doTest(ctx);
        }
        return null;
    }
}
