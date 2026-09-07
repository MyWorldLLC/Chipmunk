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

import chipmunk.runtime.MethodBinding;
import chipmunk.vm.hazel.Fiber;
import chipmunk.vm.hazel.Instruction;
import chipmunk.vm.hazel.TypeError;
import chipmunk.vm.hazel.Value;

public class Bind extends Instruction {

    protected final String method;

    public Bind(int sp, String method) {
        super(sp);
        this.method = method;
    }

    @Override
    public int apply(Fiber fiber, int ip, int bp) {
        var targetPtr = fiber.stack[bp + sp - 1];
        if(Value.isPointer(targetPtr)){
            var heap = fiber.vm().heap();
            var target = heap.read(targetPtr);
            fiber.stack[bp + sp - 1] = heap.allocateAndWrite(new MethodBinding(target, method));
        }else{
            throw new TypeError(fiber, "Object instance required for bind");
        }
        return ip + 1;
    }
}
