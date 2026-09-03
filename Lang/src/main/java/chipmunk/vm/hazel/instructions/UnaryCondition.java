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
import chipmunk.vm.hazel.Value;

public class UnaryCondition extends Instruction {

    public static final int COND_TRUE = 0;
    public static final int COND_NOT = 1;
    public static final int COND_NULL = 2; // TODO - don't think there's any way to use this - null will probably always be checked with pointer equality

    protected final int condition;
    protected final int target;

    public UnaryCondition(int sp, int condition){
        this(sp, condition, Integer.MIN_VALUE);
    }

    public UnaryCondition(int sp, int condition, int target) {
        super(sp);
        this.condition = condition;
        this.target = target;
    }

    @Override
    public final int apply(Fiber fiber, int ip, int bp) {
        var stack = fiber.stack;
        var a = stack[bp + sp];
        boolean result = false;
        if (chipmunk.vm.hazel.Value.isNumber(a)) {
            result = switch (condition) {
                case COND_TRUE -> Value.isNumber(a) && a != 0.0; // TODO - object truth
                case COND_NOT -> !(Value.isNumber(a) && a != 0.0); // TODO - object truth
                case COND_NULL -> Value.isPointer(a) && Value.isNullPointer(a);
                default -> false;
            };
        }else{
            // TODO - object truth & branch
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
