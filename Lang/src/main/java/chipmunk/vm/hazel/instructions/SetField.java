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

import chipmunk.vm.hazel.Fiber;
import chipmunk.vm.hazel.Instruction;
import chipmunk.vm.hazel.invoke.Invoker;

public class SetField extends FieldInstruction {

    protected final String field;

    public SetField(int sp, Invoker invoker, String field) {
        super(sp, invoker);
        this.field = field;
    }

    @Override
    public int apply(Fiber fiber, int ip, int bp) {
        var targetPtr = fiber.stack[bp + sp - 2];
        var target = fiber.vm().heap().read(targetPtr);
        fiber.stack[bp + sp - 2] = getFieldInvoker(targetPtr, target, fiber, field).invokeSet(fiber, bp, sp);
        return ip + 1;
    }
}
