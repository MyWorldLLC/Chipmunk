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

    public record DebugEntry(int beginIp, int endIp, int line) {}

    public record ExceptionBlock(int beginIp, int endIp, int catchIp, int exceptionLocalIndex) {}

    protected final CModule module;
    protected final Instruction[] code;
    protected DebugEntry[] debugTable;
    protected ExceptionBlock[] exceptionTable;
    protected final int argCount;
    protected final int localCount;
    protected final int defaultArgCount;
    protected final int maxStack;

    public CMethod(CModule module, String name, Instruction[] code, int argCount, int localCount, int defaultArgCount, int maxStack) {
        super(name);
        this.module = module;
        this.code = code;
        this.argCount = argCount;
        this.localCount = localCount;
        this.defaultArgCount = defaultArgCount;
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

    public int defaultArgCount() {
        return defaultArgCount;
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

    public DebugEntry[] debugTable() {
        return debugTable;
    }

    public void debugTable(DebugEntry[] debugTable) {
        this.debugTable = debugTable;
    }

    public ExceptionBlock[] exceptionTable() {
        return exceptionTable;
    }

    public void exceptionTable(ExceptionBlock[] exceptionTable) {
        this.exceptionTable = exceptionTable;
    }
}
