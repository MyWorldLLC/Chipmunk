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

package chipmunk.runtime;

import chipmunk.vm.hazel.*;
import chipmunk.vm.hazel.invoke.Linker;
import chipmunk.vm.hazel.invoke.MethodInvoker;

public class CMethodBinding extends HostCObject implements GCCollectable {

    protected final double target;
    protected final String methodName;
    protected final Linker linker;

    protected MethodInvoker method;
    protected int callIndex;
    protected double[] args;

    public CMethodBinding(double target, String methodName, Linker linker) {
        this.target = target;
        this.methodName = methodName;
        this.linker = linker;
        args = new double[0];
    }

    public void bindArgs(int callIndex, double[] args) {
        this.callIndex = callIndex;
        this.args = args;
    }

    public double target(){
        return target;
    }

    public String methodName(){
        return methodName;
    }

    public int callIndex(){
        return callIndex;
    }

    public double[] args(){
        return args;
    }

    protected MethodInvoker getMethodInvoker(double targetPtr, Object target, Fiber fiber, String name, int args) {
        if(method != null && method.canInvoke(target)){
            return method;
        }
        method = linker.methodInvokerFor(fiber, targetPtr, name, args);
        return method;
    }

    public int dynamicCall(Fiber fiber, int ip, int bp, int sp, String methodName, int args){
        var ptr = fiber.stack[bp + sp - args];
        var heap = fiber.vm().heap();
        if(!Value.isPointer(ptr)) throw new TypeError(fiber, "Not a reference to an object: " + Value.toString(ptr) + "." + methodName + "(" + args + ")");
        var obj = heap.read(ptr);
        return getMethodInvoker(ptr, obj, fiber, methodName, args).invokeMethod(fiber, ip, bp, sp, obj);
    }

    @Override
    public void gcVisit(GarbageCollector.GCCollection collection) {
        collection.visitStorage(args);
    }
}
