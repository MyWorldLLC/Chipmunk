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

package chipmunk.vm.hazel;

import chipmunk.runtime.CClass;
import chipmunk.runtime.CMethod;
import chipmunk.runtime.CModule;

public abstract class Instruction {

    protected final int sp;

    protected Instruction(int sp) {
        this.sp = sp;
    }

    public int sp(){
        return sp;
    }

    public abstract int apply(Fiber fiber, int ip, int bp);

    /*public int dynamicCall(Fiber fiber, int ip, int bp, int sp, String methodName, int args){
        var ptr = fiber.stack[bp + sp - args];
        var heap = fiber.vm().heap();
        var obj = heap.read(Value.getPointer(ptr));
        //System.out.println("Dispatching call to " + Value.pointerToString(ptr) + "::" + methodName + "(" + args + ")");
        if(obj instanceof double[] ins){
            // TODO - need to add ability to cache so we don't have to do a full search every time.
            var cls = (CClass) heap.read(Value.getPointer(ins[0]));
            var methods = cls.instanceMethodDefs();
            CMethod method = null;
            for(int i = 0; i < methods.length; i++){
                var m = methods[i];
                if(m.argCount() == args && m.name().equals(methodName)){
                    method = methods[i];
                    break;
                }
            }
            if(method != null){
                // TODO - this is going to be handled by an invoker per-instruction
                return fiber.vm().invokeMethod(method, fiber, ip, bp, sp);
            }else{
                // TODO - method not found
                System.out.println(
                        "Method not found: " + methodName
                );
            }
        }else{
            // TODO - native calls
            //System.out.println("Value at " + Value.pointerToString(ptr) + " is a native object: " + obj);
            if(obj instanceof CModule m){
                //System.out.println(methodName + "(" + args + ")");
                var method = m.getMethod(methodName, args);
                if(method != null){
                    //System.out.println("Invoking " + Value.pointerToString(ptr) + "::" + methodName);
                    return invokeMethod(method, fiber, ip, bp, sp);
                }else if(methodName.equals("getModule")){
                    fiber.stack[bp + sp - args] = m.selfPtr();
                    //System.out.println("Returning self");
                }else{

                    System.out.println(m + "::" + methodName + "(" + args + ") not found");
                }
            }
        }
        //System.out.println("Done calling " + Value.pointerToString(ptr) + "::" + methodName);
        return ip + 1;
    }

    public static final int invokeMethod(CMethod method, Fiber fiber, int ip, int bp, int sp){
        var callingFrame = fiber.currentFrame();
        //System.out.println("Calling frame before invoking " + method.name() + ": " + fiber.vm().dumpStack(fiber, bp, 5));
        callingFrame.ip = ip + 1; // Resume at next instruction following this one
        fiber.pushCallFrame(method, bp + sp - method.argCount());
        // This causes the interpreter to transfer control to the outer interpreter loop, where it will reset ip & bp
        // and transfer control to the newly called method.
        return Fiber.RETURN_SIGNAL;
    }*/

    public String toString() {
        return getClass().getSimpleName() + " sp=" + sp;
    }
}
