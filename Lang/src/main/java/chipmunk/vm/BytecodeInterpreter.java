/*
 * Copyright (C) 2025 MyWorld, LLC
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

package chipmunk.vm;

import chipmunk.ChipmunkRuntimeException;
import chipmunk.runtime.CList;
import chipmunk.runtime.CMap;
import chipmunk.runtime.CMethod;
import chipmunk.runtime.Suspend;

import java.math.BigDecimal;

import static chipmunk.vm.Opcodes.*;

public final class BytecodeInterpreter {

    public static void run(Fiber fiber){
        var suspension = fiber.resumeFrame();
        dispatch(fiber, suspension.method(), suspension.ip());
    }

    public static void dispatch(Fiber fiber, CMethod method, int ip){

        var code = method.code;

        try{

            // If there are still suspended frames, recurse down to resume call state
            if(fiber.hasSuspension()){
                var suspension = fiber.resumeFrame();
                dispatch(fiber, suspension.method(), suspension.ip());
            }
            // TODO - ip must be set before dispatching instruction so that suspend/resume work properly

            dispatch:
            while(ip < code.length){
                try{
                    switch (code[ip]){
                        case GETLOCAL -> {
                            fiber.push(fiber.getLocal(fetchByte(code, ip + 1)));
                            ip += 2;
                        }
                        case SETLOCAL -> {
                            fiber.setLocal(fetchByte(code, ip + 1), fiber.pop());
                            ip += 2;
                        }
                        case POP -> {
                            fiber.pop();
                            ip++;
                        }
                        case DUP -> {
                            fiber.push(fiber.peek());
                            ip++;
                        }
                        case ADD -> ip = binaryOp("plus", fiber, method, ip);
                        case SUB -> ip = binaryOp("minus", fiber, method, ip);
                        case MUL -> ip = binaryOp("mul", fiber, method, ip);
                        case DIV -> ip = binaryOp("div", fiber, method, ip);
                        case FDIV -> ip = binaryOp("fdiv", fiber, method, ip);
                        case MOD -> ip = binaryOp("mod", fiber, method, ip);
                        case POW -> ip = binaryOp("pow", fiber, method, ip);
                        case NEG -> ip = unaryOp("neg", fiber, method, ip);
                        case POS -> ip = unaryOp("pos", fiber, method, ip);
                        case IF -> {
                            var t = (Boolean) fiber.invokeValue(method, ip, "truth", 0, true);
                            ip = testJump(!t, fiber, ip, fetchInt(code, ip + 1), 5);
                        }
                        case TRUTH -> {
                            var t = (Boolean) fiber.invokeValue(method, ip, "truth", 0, true);
                            fiber.push(t);
                            ip++;
                        }
                        case NOT -> {
                            var t = (Boolean) fiber.invokeValue(method, ip, "truth", 0, true);
                            fiber.push(!t);
                            ip++;
                        }
                        case PUSH -> {
                            fiber.push(method.module.constants[fetchInt(code, ip + 1)]);
                            ip += 5;
                        }
                        case LT -> {
                            var v = (Number) fiber.invokeValue(method, ip, "compare", 1, true);
                            fiber.push(v.intValue() < 0);
                            ip++;
                            //ip = testJump(v.intValue() < 0, fiber, ip, fetchInt(code, ip + 1), 5);
                        }
                        case LE -> {
                            var v = (Number) fiber.invokeValue(method, ip, "compare", 1, true);
                            fiber.push(v.intValue() <= 0);
                            ip++;
                            //ip = testJump(v.intValue() <= 0, fiber, ip, fetchInt(code, ip + 1), 5);
                        }
                        case GOTO -> ip = checkSuspend(fiber, ip, fetchInt(code, ip + 1));
                        case RETURN -> {
                            break dispatch;
                        }
                        case CALLAT -> {
                            var pCount = fetchByte(code, ip + 1);
                            var name = (String) method.module.constants[fetchInt(code, ip + 2)];
                            // pre-adjust and then subtract for the call so we don't have to do anything special to account
                            // for the ip when suspensions happen
                            ip += 6;
                            fiber.invoke(method, ip - 6, name, pCount, false);
                        }
                        case LIST -> {
                            fiber.push(new CList(fetchInt(code, ip + 1)));
                            ip += 5;
                        }
                        case MAP -> {
                            fiber.push(new CMap(fetchInt(code, ip + 1)));
                            ip += 5;
                        }
                        case GETAT -> {
                            fiber.invoke(method, ip, "getAt", 1, false);
                            ip++;
                        }
                        case SETAT -> {
                            fiber.invoke(method, ip, "setAt", 2, false);
                            ip++;
                        }
                        default -> throw new RuntimeException("Invalid opcode 0x%02X".formatted(code[ip]));
                    }
                }catch (Suspend s){
                    // Throw suspensions to the outer handler
                    throw s;
                }
                catch (Exception e) {
                    // TODO - dispatch to exception handling code
                    throw e;
                }
            }
        }catch (Suspend s){
            fiber.suspendFrame(method, ip);
            throw s;
        }
    }

    public static int ternaryOp(String name, Fiber fiber, CMethod method, int ip){
        fiber.invoke(method, ip, name, 2, true);
        return ip + 1;
    }

    public static int binaryOp(String name, Fiber fiber, CMethod method, int ip){
        fiber.invoke(method, ip, name, 1, true);
        return ip + 1;
    }

    public static int unaryOp(String name, Fiber fiber, CMethod method, int ip){
        fiber.invoke(method, ip, name, 0, true);
        return ip + 1;
    }

    public static int testJump(boolean test, Fiber fiber, int ip, int jmpTarget, int ipInc) throws Suspend {
        if(!test){
            return ip + ipInc;
        }
        return checkSuspend(fiber, ip, jmpTarget);
    }

    public static int checkSuspend(Fiber fiber, int ip, int target) throws Suspend {
        if(target < ip && fiber.interrupted()){
            throw new Suspend();
        }
        return target;
    }

}
