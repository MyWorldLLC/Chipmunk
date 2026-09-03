/*
 * Copyright (C) 2026 MyWorld, LLC
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

package chipmunk.vm.hazel.instructions;

import chipmunk.vm.OpcodeNames;
import chipmunk.vm.hazel.Fiber;
import chipmunk.vm.hazel.Instruction;
import chipmunk.vm.hazel.Value;

public class If extends Instruction {

    protected final int target;

    public If(int sp, int target) {
        super(sp);
        this.target = target;
    }

    @Override
    public final int apply(Fiber fiber, int ip, int bp) {
        var stack = fiber.stack;
        var a = stack[bp + sp];
        boolean result = false;
        if(Value.isNumber(a)) {
            result = a != 0.0;
        }else{
            dynamicCall(bp + sp, a, OpcodeNames.TRUTH, 0);
        }

        if(target != Integer.MIN_VALUE){
            // Note that branches use inverse of result - if the condition does not hold, the branch is taken
            if(!result){
                return target;
            }
        }else{
            stack[bp + sp - 1] = result ? 1.0 : 0.0;
        }
        return ip + 1;
    }
}
