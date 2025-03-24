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

import static chipmunk.vm.tree.Conversions.*;

public class For implements Node {
    public Node pre;
    public Node test;
    public Node body;
    public Node post;

    @Override
    public Object execute(Fiber ctx) {
        doPre(ctx);
        return doBody(ctx, doTest(ctx));
    }

    public Object doPre(Fiber ctx) {
        if (pre != null) {
            try {
                pre.execute(ctx);
            } catch (Exception e) {
                ctx.suspendStateless(e, (c, s) -> doTest(c));
            }
        }
        return null;
    }

    public boolean doTest(Fiber ctx) {
        try {
            return test.executeBoolean(ctx);
        } catch (Exception e) {
            ctx.suspendStateless(e, (c, s) -> doBody(c, toBoolean(s)));
        }
        return false; // suspend() will rethrow so this will never be reached
    }

    public Object doBody(Fiber ctx, boolean test) {
        var t = test;
        while (t && !ctx.checkInterrupt()) {
            try {
                body.execute(ctx);
            } catch (Exception e) {
                ctx.suspendStateless(e, this::doPost);
            }

            doPost(ctx, 0);
            t = doTest(ctx);
        }
        return 0;
    }

    public Object doPost(Fiber ctx, Object prior) {
        if (post != null) {
            try {
                post.execute(ctx);
            } catch (Exception e) {
                ctx.suspendStateless(e, (c, s) -> doTest(c));
            }
        }
        return null;
    }

}
