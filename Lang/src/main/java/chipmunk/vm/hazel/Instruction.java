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

public abstract class Instruction {

    protected final int sp;

    protected Instruction(int sp) {
        this.sp = sp;
    }

    public int sp(){
        return sp;
    }

    public abstract int apply(Fiber fiber, int ip, int bp);

    public final int dynamicCall(Fiber fiber, int ip, int bp, int sp, double ptr, String methodName, int args){
        var heap = fiber.vm().heap();
        var obj = heap.read(Value.getPointer(ptr));
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
                fiber.pushCallFrame(method, 0, bp, sp);
                // This causes the interpreter to transfer control to the outer interpreter loop, where it will reset ip & bp
                // and transfer control to the newly called method.
                return Fiber.RETURN_SIGNAL;
            }else{
                // TODO - method not found
            }
        }else{
            // TODO - native calls
        }
        return ip + 1;
    }

    public String toString() {
        return getClass().getSimpleName() + " sp=" + sp;
    }
}
