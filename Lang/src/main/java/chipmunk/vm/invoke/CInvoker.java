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

package chipmunk.vm.invoke;

import chipmunk.runtime.CClass;
import chipmunk.runtime.CObject;
import chipmunk.runtime.Fiber;
import chipmunk.runtime.Signature;

public class CInvoker implements Invoker {

    protected final int frameLocals;
    protected final CClass targetType;
    protected final Signature signature;
    protected final int method;
    protected final int[] traitChain;
    protected final CClass[] chainTypes;

    public CInvoker(int frameLocals, CClass targetType, Signature signature, int method) {
        this(frameLocals, targetType, signature, method, null, null);
    }

    public CInvoker(int frameLocals, CClass targetType, Signature signature, int method, int[] traitChain, CClass[] chainTypes) {
        this.frameLocals = frameLocals;
        this.targetType = targetType;
        this.signature = signature;
        this.method = method;
        this.traitChain = traitChain;
        this.chainTypes = chainTypes;
        if(traitChain != null && traitChain.length != chainTypes.length){
            throw new IllegalArgumentException("Trait chain and type arrays must be same length");
        }
    }

    @Override
    public Object invoke(Fiber fiber, Object target, int argCount, Object... args) throws Throwable {
        var cTarget = getTarget(target);

        // TODO - target will be null if any chained trait types have changed.


        var m = cTarget.cls.getInstanceMethod(method);

        fiber.preCall(frameLocals);
        try{
            // TODO
            //return m.code.execute(fiber);
            return null;
        }finally {
            fiber.postCall();
        }
    }

    public boolean validate(Fiber fiber, Object target, int argCount) {
        var cTarget = getTarget(target);
        if(!cTarget.cls.equals(((CObject) target).cls) || argCount != signature.getArgCount()){
            return false;
        }
        // Generally, validation means:
        // (1) Does the bound type matched the passed target type, and
        // (2) Is this either (a) not a trait call, or (b) the chained trait types match?
        // TODO - validate trait chain
        return true;
    }

    protected CObject getTarget(Object target){
        var cTarget = (CObject) target;
        if(traitChain != null){
            for (int trait : traitChain) {
                cTarget = (CObject) cTarget.getField(trait);
                if(cTarget == null || !cTarget.cls.equals(chainTypes[trait])){
                    return null;
                }
            }
        }
        if(!cTarget.cls.equals(targetType)){
            // TODO - throw because the target didn't resolve properly
        }
        return cTarget;
    }
}
