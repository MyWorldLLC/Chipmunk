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

public class BinaryCondition extends Instruction {

    public static final int COND_LT = 0;
    public static final int COND_LE = 1;
    public static final int COND_EQ = 2;
    public static final int COND_NE = 3;
    public static final int COND_GE = 4;
    public static final int COND_GT = 5;
    public static final int COND_IS = 6;
    public static final int COND_INSTANCEOF = 7;

    protected final int condition;
    protected final int target;

    public BinaryCondition(int sp, int condition){
        this(sp, condition, Integer.MIN_VALUE);
    }

    public BinaryCondition(int sp, int condition, int target) {
        super(sp);
        this.condition = condition;
        this.target = target;
    }

    @Override
    public final int apply(Fiber fiber, int ip, int bp) {
        var stack = fiber.stack;
        var a = stack[bp + sp - 1];
        var b = stack[bp + sp];
        boolean result = false;
        if(Value.isNumber(a) && Value.isNumber(b)) {
            result = switch (condition) {
                case COND_LT -> a < b;
                case COND_LE -> a <= b;
                case COND_EQ -> a == b;
                case COND_NE -> a != b;
                case COND_GE -> a >= b;
                case COND_GT -> a > b;
                case COND_IS -> a == b;
                case COND_INSTANCEOF -> false; // TODO
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

    @Override
    public String toString() {
        var cond = switch (condition){
            case COND_LT -> "<";
            case COND_LE -> "<=";
            case COND_EQ -> "==";
            case COND_NE -> "!=";
            case COND_GE -> ">=";
            case COND_GT -> ">";
            case COND_IS -> "is";
            case COND_INSTANCEOF -> "instanceof";
            default -> "<invalid>";
        };
        return super.toString() + " " + cond + " " + target;
    }
}
