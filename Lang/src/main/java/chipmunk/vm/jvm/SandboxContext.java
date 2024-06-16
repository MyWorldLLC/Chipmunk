/*
 * Copyright (C) 2023 MyWorld, LLC
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

package chipmunk.vm.jvm;

import chipmunk.vm.invoke.security.LinkingPolicy;

public class SandboxContext {

    protected final String className;
    protected final String methodName;
    protected final String methodDescriptor;
    protected final LinkingPolicy policy;
    protected final TrapConfig trapConfig;

    public SandboxContext(String className, String methodName, String methodDescriptor, LinkingPolicy policy, TrapConfig trapConfig){
        this.className = className;
        this.methodName = methodName;
        this.methodDescriptor = methodDescriptor;
        this.policy = policy;
        this.trapConfig = trapConfig;
    }

    public SandboxContext(String className, String methodName, String methodDescriptor, LinkingPolicy policy){
        this(className, methodName, methodDescriptor, policy, new TrapConfig());
    }

    public String getClassName(){
        return className;
    }

    public String getMethodName(){
        return methodName;
    }

    public String getMethodDescriptor(){
        return methodDescriptor;
    }

    public LinkingPolicy getLinkingPolicy(){
        return policy;
    }

    public TrapConfig getTrapConfig(){
        return trapConfig;
    }

}
