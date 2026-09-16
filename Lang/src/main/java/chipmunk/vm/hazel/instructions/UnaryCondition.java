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
import chipmunk.vm.hazel.Value;
import chipmunk.vm.hazel.invoke.Linker;

public class UnaryCondition extends CallingInstruction {

    public static final int NO_JUMP = Integer.MIN_VALUE;

    public static final int COND_TRUE = 0;
    public static final int COND_NOT = 1;

    protected final int condition;
    protected final int target;

    public UnaryCondition(int sp, Linker linker, int condition){
        this(sp, linker, condition, NO_JUMP);
    }

    public UnaryCondition(int sp, Linker linker, int condition, int target) {
        super(sp, linker);
        this.condition = condition;
        this.target = target;
    }

    @Override
    public final int apply(Fiber fiber, int ip, int bp) {
        var stack = fiber.stack;
        var a = stack[bp + sp - 1];
        boolean result = false;
        if (Value.isNumber(a)) {
            result = switch (condition) {
                case COND_TRUE -> Value.isNumber(a) && a != 0.0;
                case COND_NOT -> !(Value.isNumber(a) && a != 0.0);
                default -> false;
            };
        }else if(Value.isNullPointer(a)){
            result = false;
        }else{
            fiber.continueWith((f, frame) -> {
                var dValue = stack[bp + sp - 1];
                var cResult = switch (condition) {
                    case COND_TRUE -> Value.isTruthy(dValue);
                    case COND_NOT -> !Value.isTruthy(dValue);
                    default -> false;
                };
                frame.continuation = null;
                frame.ip = handleResult(ip, stack, bp, cResult);
            });
            return dynamicCall(fiber, ip, bp, sp, OpcodeNames.TRUTH, 1);
        }

        return handleResult(ip, stack, bp, result);
    }

    private int handleResult(int ip, double[] stack, int bp, boolean result){
        if(target != NO_JUMP){
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
