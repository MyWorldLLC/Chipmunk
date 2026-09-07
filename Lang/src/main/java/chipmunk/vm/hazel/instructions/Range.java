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

import chipmunk.runtime.CRange;
import chipmunk.vm.hazel.Fiber;
import chipmunk.vm.hazel.Instruction;

public class Range extends Instruction {

    protected final boolean inclusive;

    public Range(int sp, boolean inclusive) {
        super(sp);
        this.inclusive = inclusive;
    }

    @Override
    public int apply(Fiber fiber, int ip, int bp) {
        var start = fiber.stack[bp + sp - 2];
        var end = fiber.stack[bp + sp - 1];
        fiber.stack[bp + sp] = fiber.vm().heap().allocateAndWrite(new CRange(start, end, 1, inclusive));
        return ip + 1;
    }
}
