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
import chipmunk.vm.hazel.invoke.FieldInvoker;
import chipmunk.vm.hazel.invoke.Invoker;

public abstract class FieldInstruction extends Instruction {

    protected final Invoker invoker;
    protected FieldInvoker field;

    public FieldInstruction(int sp, Invoker invoker) {
        super(sp);
        this.invoker = invoker;
    }

    protected FieldInvoker getFieldInvoker(double targetPtr, Object target, Fiber fiber, String name) {
        if(field != null && field.canInvoke(target)){
            return field;
        }
        field = invoker.fieldInvokerFor(fiber, targetPtr, name);
        return field;
    }
}
