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

public class Add extends Instruction {

    public Add(int sp) {
        super(sp);
    }

    @Override
    public final int apply(Fiber fiber, int ip, int bp) {
        var stack = fiber.stack;
        var a = stack[bp + sp - 1];
        var b = stack[bp + sp];
        if(Value.isNumber(a) && Value.isNumber(b)) {
            stack[bp + sp - 1] = a + b;
        }else{
            dynamicCall(fiber, bp + sp - 1, a, OpcodeNames.ADD, 1);
        }
        return ip + 1;
    }

}
