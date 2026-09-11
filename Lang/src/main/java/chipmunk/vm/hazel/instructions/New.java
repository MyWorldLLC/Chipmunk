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

import chipmunk.runtime.CClass;
import chipmunk.vm.hazel.Fiber;
import chipmunk.vm.hazel.invoke.Linker;

public class New extends CallingInstruction {

    protected final int argCount;

    public New(int sp, Linker linker, int argCount) {
        super(sp, linker);
        this.argCount = argCount;
    }

    @Override
    public int apply(Fiber fiber, int ip, int bp) {
        var heap = fiber.vm().heap();
        var clsPtr = fiber.stack[bp + sp - argCount];
        var cls = (CClass) heap.read(clsPtr);
        var insPtr = heap.allocateAndWrite(cls.createInstance(fiber.vm()));
        fiber.stack[bp + sp - argCount] = insPtr; // Replace the class with the new instance, and call the constructor.
        return dynamicCall(fiber, ip, bp, sp, "$" + cls.name(), argCount);
    }
}
