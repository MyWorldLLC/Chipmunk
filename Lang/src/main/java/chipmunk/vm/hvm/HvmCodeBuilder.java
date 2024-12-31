/*
 * Copyright (C) 2024 MyWorld, LLC
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

package chipmunk.vm.hvm;

import chipmunk.binary.BinaryMethod;
import chipmunk.compiler.assembler.Operands;
import myworld.hummingbird.Executable;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.Symbol;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class HvmCodeBuilder {

    protected final Executable.Builder builder;
    protected final BinaryMethod method;
    protected final IpRemapping remapping;
    protected final RegisterStates registerStates;
    protected final Operands operands;
    protected final TosStates tosStates;

    protected final Map<Integer, Supplier<Opcode>> deferredOps;
    protected final Map<Integer, Supplier<Symbol>> deferredSymbols;

    private int ip;
    private int lastIp;

    public HvmCodeBuilder(BinaryMethod method){
        this.builder = Executable.builder();
        this.method = method;
        remapping = new IpRemapping();
        registerStates = new RegisterStates(); // Currently used only by get/set local
        operands = new Operands(method.getArgCount() + method.getLocalCount());
        tosStates = new TosStates();
        deferredOps = new HashMap<>();
        deferredSymbols = new HashMap<>();
        ip = 0;
    }

    public Operands operands(){
        return operands;
    }

    public IpRemapping remapping(){
        return remapping;
    }

    public RegisterStates registerStates(){
        return registerStates;
    }

    public int appendData(byte[] data){
        return builder.appendData(data);
    }

    public int appendSymbol(Symbol symbol){
        return builder.appendSymbol(symbol);
    }

    public int appendOpcode(Opcode op){
        var hIp = builder.appendOpcode(op);
        remapping.remap(lastIp, ip, hIp);
        tosStates.markOpcode(hIp, op.dst());
        return hIp;
    }

    public void markJump(int target){
        markJump(target, getTos());
    }

    public void markJump(int target, int tos){
        tosStates.markOpcode(target, tos);
    }

    public int getTos(){
        var reg = tosStates.getTosRegister(ip);
        if(reg == -1){
            reg = operands.currentTos();
        }
        return reg;
    }

    public int deferOpcode(Supplier<Opcode> factory){
        var hIp = appendOpcode(new Opcode(0xFF));
        deferredOps.put(hIp, factory);
        return hIp;
    }

    public int deferSymbol(Supplier<Symbol> factory){
        var index = builder.appendSymbol(Symbol.empty("placeholder"));
        deferredSymbols.put(index, factory);
        return index;
    }

    public Executable build(){
        for(var entry : deferredOps.entrySet()){
            builder.replaceOpcode(entry.getKey(), entry.getValue().get());
        }
        for(var entry : deferredSymbols.entrySet()){
            builder.replaceSymbol(entry.getKey(), entry.getValue().get());
        }
        return builder.build();
    }

    public boolean hasNextOp(){
        return ip < method.getCode().length;
    }

    public byte nextOp(){
        lastIp = ip;
        var opcode = method.getCode()[ip];
        ip++;
        return opcode;
    }

    public byte peekNextOp(){
        return method.getCode()[ip + 1];
    }

    public int fetchInt(){
        var instructions = method.getCode();
        int b1 = instructions[ip] & 0xFF;
        int b2 = instructions[ip + 1] & 0xFF;
        int b3 = instructions[ip + 2] & 0xFF;
        int b4 = instructions[ip + 3] & 0xFF;
        ip += 4;
        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }

    public Object[] constants(){
        return method.getConstantPool();
    }

    public Object constant(int index){
        return constants()[index];
    }

    public int remapIp(int cIp){
        return remapping.find(cIp).h();
    }

    // TODO - constant tracking (avoid redundant CONST)

}
