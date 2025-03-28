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

package chipmunk.runtime;

import chipmunk.vm.tree.nodes.FunctionNode;

public class CMethod {

    public String name;
    public int argCount;
    public FunctionNode code;

    public Object hostCall(Fiber ctx, Object... args){
        if(args.length != argCount){
            throw new IllegalArgumentException("Argument count mismatch: expected %d got %d".formatted(argCount, args.length));
        }
        ctx.preCall(0);
        for(int i = 0; i < args.length; i++){
            ctx.setLocal(i, args[i]);
        }
        try{
            return code.execute(ctx);
        }finally {
            ctx.postCall();
        }
    }

}
