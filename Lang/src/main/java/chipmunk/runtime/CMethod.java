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

import chipmunk.vm.hazel.Instruction;

public class CMethod extends NamedHostObject {

    protected final CModule module;
    protected final Instruction[] code;
    protected final int argCount;
    protected final int localCount;
    protected final int maxStack;

    protected byte[] origCode;

    public CMethod(CModule module, String name, Instruction[] code, int argCount, int localCount, int maxStack) {
        super(name);
        this.module = module;
        this.code = code;
        this.argCount = argCount;
        this.localCount = localCount;
        this.maxStack = maxStack;
    }

    public CModule module() {
        return module;
    }

    public Instruction[] code() {
        return code;
    }

    public int argCount() {
        return argCount;
    }

    public int localCount() {
        return localCount;
    }

    public int maxStack() {
        return maxStack;
    }

    public String dumpCode(){
        var builder = new StringBuilder();
        for(int i = 0; i < code.length; ++i){
            builder.append(i + ": " + code[i] + "\n");
        }
        return builder.toString();
    }

    public byte[] getOriginalCode() {
        return origCode;
    }

    public void setOriginalCode(byte[] origCode) {
        this.origCode = origCode;
    }
}
