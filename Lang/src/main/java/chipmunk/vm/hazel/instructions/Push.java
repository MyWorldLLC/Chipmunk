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

public class Push extends Instruction {

    protected final int constantIndex;

    public Push(int sp, int constantIndex) {
        super(sp);
        this.constantIndex = constantIndex;
    }

    public int constantIndex() {
        return constantIndex;
    }

    @Override
    public final int apply(Fiber fiber, int ip, int bp) {
        var value = fiber.constants()[constantIndex];
        switch (value){
            case Number n -> fiber.stack[bp + sp] = n.doubleValue();
            default -> {
                // TODO - get value, convert, allocate a pointer on heap, and push the pointer
            }
        }
        return ip + 1;
    }

    @Override
    public String toString() {
        return super.toString() + " " + constantIndex;
    }
}
