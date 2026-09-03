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

public class LocalGet extends Instruction {

    protected final int local;

    public LocalGet(int sp, int local) {
        super(sp);
        this.local = local;
    }

    public int local() {
        return local;
    }

    @Override
    public final int apply(Fiber fiber, int ip, int bp) {
        //System.out.println("Getting " + local);
        fiber.stack[bp + sp] = fiber.stack[bp + local];
        return ip + 1;
    }

    @Override
    public String toString() {
        return super.toString() + " local=" + local;
    }
}
