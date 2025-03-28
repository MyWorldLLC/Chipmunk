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

import chipmunk.runtime.Suspend;
import chipmunk.runtime.Upvalue;
import chipmunk.runtime.Fiber;

public class SetUpvalue extends SetVar {

    public SetUpvalue(int v) {
        super(v);
    }

    @Override
    public Object execute(Fiber ctx) {
        Object result;
        try{
            result = value.execute(ctx);
        }catch (Suspend s){
            ctx.suspendStateless(s, (c, prior) -> ((Upvalue) ctx.getLocal(v)).set(prior));
            throw s;
        }
        return ((Upvalue) ctx.getLocal(v)).set(result);
    }
}
