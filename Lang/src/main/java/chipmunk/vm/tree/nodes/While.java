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

import chipmunk.vm.tree.Fiber;
import chipmunk.vm.tree.Node;

import static chipmunk.vm.tree.Conversions.toBoolean;
import static chipmunk.vm.tree.Conversions.toInt;

public class While implements Node {
    public Node test;
    public Node body;

    @Override
    public Object execute(Fiber ctx) {
        return doBody(ctx, doTest(ctx, 0));
    }

    public boolean doTest(Fiber ctx, Object prior) {
        try {
            return test.executeBoolean(ctx);
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

            try {
                t = test.executeBoolean(ctx);
            } catch (Exception e) {
                ctx.suspendStateless(e, (c, s) -> doBody(c, toBoolean(s)));
            }
        }
        return 0;
    }
}
