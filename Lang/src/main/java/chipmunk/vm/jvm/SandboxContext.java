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

import java.util.Set;

public class SandboxContext {

    protected final String className;
    protected final String methodName;
    protected final String methodDescriptor;

    protected final JvmCompilerConfig config;

    public SandboxContext(String className, String methodName, String methodDescriptor, JvmCompilerConfig config){
        this.className = className;
        this.methodName = methodName;
        this.methodDescriptor = methodDescriptor;
        this.config = config;
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

    public JvmCompilerConfig getCompilerConfig(){
        return config;
    }

    public LinkingPolicy getLinkingPolicy(){
        return config.getLinkingPolicy();
    }

    public TrapConfig getTrapConfig(){
        return config.getTrapConfig();
    }

    public Set<Class<? extends Throwable>> getUncatchable(){
        return config.getUncatchable();
    }

}
