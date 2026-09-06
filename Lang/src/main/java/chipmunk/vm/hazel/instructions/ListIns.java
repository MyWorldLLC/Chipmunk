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

import java.util.ArrayList;

public class ListIns extends Instruction {

    protected final int elements;

    public ListIns(int sp, int elements) {
        super(sp);
        this.elements = elements;
    }

    @Override
    public int apply(Fiber fiber, int ip, int bp) {
        fiber.stack[bp + sp] = fiber.vm().heap().allocateAndWrite(new ArrayList<>(elements));
        return ip + 1;
    }
}
