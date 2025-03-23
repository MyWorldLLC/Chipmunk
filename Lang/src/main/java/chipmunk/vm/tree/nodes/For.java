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

import chipmunk.vm.tree.Context;
import chipmunk.vm.tree.Node;

public class For implements Node {
    public Node pre;
    public Node test;
    public Node body;
    public Node post;

    @Override
    public long execute(Context ctx) {
        doPre(ctx, 0);
        return doBody(ctx, doTest(ctx, 0));
    }

    public int doPre(Context ctx, int prior) {
        if (pre != null) {
            try {
                pre.execute(ctx);
            } catch (Exception e) {
                ctx.suspendStateless(e, (ctx1, prior1) -> doTest(ctx1, prior1));
            }
        }
        return 0;
    }

    public long doTest(Context ctx, long prior) {
        try {
            return test.execute(ctx);
        } catch (Exception e) {
            ctx.suspendStateless(e, this::doBody);
        }
        return 0; // suspend() will rethrow so this will never be reached
    }

    public long doBody(Context ctx, long test) {
        long t = test;
        while (t != 0 && !ctx.checkInterrupt()) {
            try {
                body.execute(ctx);
            } catch (Exception e) {
                ctx.suspendStateless(e, this::doPost);
            }

            doPost(ctx, 0);
            t = doTest(ctx, 0);
        }
        return 0;
    }

    public long doPost(Context ctx, long prior) {
        if (post != null) {
            try {
                post.execute(ctx);
            } catch (Exception e) {
                ctx.suspendStateless(e, this::doTest);
            }
        }
        return 0;
    }

}
