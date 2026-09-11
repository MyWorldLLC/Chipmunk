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
import chipmunk.vm.hazel.Value;
import chipmunk.vm.hazel.invoke.Linker;

public class BinaryCondition extends CallingInstruction {

    public static final int NO_JUMP = Integer.MIN_VALUE;

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

    public BinaryCondition(int sp, Linker linker, int condition){
        this(sp, linker, condition, NO_JUMP);
    }

    public BinaryCondition(int sp, Linker linker, int condition, int target) {
        super(sp, linker);
        this.condition = condition;
        this.target = target;
    }

    @Override
    public final int apply(Fiber fiber, int ip, int bp) {
        var stack = fiber.stack;
        var a = stack[bp + sp - 2];
        var b = stack[bp + sp - 1];
        boolean result;
        if(Value.isNumber(a)) {
            // Note: in the case of a type mismatch, these will always return false because every non-number is a NaN
            // and every comparison of a NaN against any value is false. All of these should fail if the LHS is a number
            // and the RHS is not, because the operation type is always determined by the LHS of an expression.
            result = switch (condition) {
                case COND_LT -> a < b;
                case COND_LE -> a <= b;
                case COND_EQ -> a == b;
                case COND_NE -> a != b;
                case COND_GE -> a >= b;
                case COND_GT -> a > b;
                case COND_IS -> a == b;
                case COND_INSTANCEOF -> false; // Always false because the RHS isn't a class pointer
                default -> false;
            };
        }else{
            // TODO - object truth & comparison
            result = switch (condition) {
                case COND_LT -> a < b;
                case COND_LE -> a <= b;
                case COND_EQ -> a == b;
                case COND_NE -> a != b;
                case COND_GE -> a >= b;
                case COND_GT -> a > b;
                case COND_IS -> Value.getPointer(a) == Value.getPointer(b);
                case COND_INSTANCEOF -> false; // TODO
                default -> false;
            };
        }

        //System.out.println("Branch for values " + Value.toString(a) + " " + Value.toString(b) + " result=" + result);
        //System.out.println("Jumping? " + (target != NO_JUMP));

        if(target != NO_JUMP){
            // Note that branches use inverse of result - if the condition does not hold, the branch is taken
            if(!result){
                //System.out.println("Jumping to target");
                return target;
            }
        }else{
            stack[bp + sp - 2] = result ? 1.0 : 0.0;
        }
        //System.out.println("Running next op");
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
