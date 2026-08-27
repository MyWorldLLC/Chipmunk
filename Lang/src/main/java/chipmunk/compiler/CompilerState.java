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

package chipmunk.compiler;

import chipmunk.compiler.symbols.Symbol;
import chipmunk.compiler.symbols.SymbolTable;

import java.lang.classfile.Label;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class CompilerState {

    public record LoopLabels(Label continueLabel, Label breakLabel) {}

    protected final Deque<LoopLabels> loopLabels = new ArrayDeque<>();

    protected SymbolTable scope;

    protected final Map<String, byte[]> classes = new HashMap<>();

    public void enterScope(SymbolTable scope){
        enterScope(scope, 0);
    }
    public void enterScope(SymbolTable scope, int preserveArgs){
        this.scope = scope;
        // When we enter a scope, initialize upvalues for any upvalue-valued locals
        // in the scope.
        /*for(var symbol : symbols.getAllSymbols()){
            if(symbol.isUpvalue()){
                var localIndex = symbols.getLocalIndex(symbol);
                if(localIndex < preserveArgs){
                    assembler.getLocal(localIndex);
                    assembler.initUpvalue(localIndex);
                    assembler.setUpvalue(localIndex);
                }else{
                    assembler.initUpvalue(localIndex);
                }
            }
        }*/
    }

    public SymbolTable scope(){
        return scope;
    }

    public void exitScope(){
        if(scope != null){
            scope = scope.getParent();
        }
    }

    public Deque<SymbolTable> getSymbolTrace(String name){
        Deque<SymbolTable> trace = new ArrayDeque<>();

        SymbolTable symTab = scope;

        boolean found = false;
        while(!found){
            trace.add(symTab);

            for(Symbol symbol : symTab.getAllSymbols()){
                if(symbol.getName().equals(name)){
                    found = true;
                    break;
                }
            }

            if(!found){
                symTab = symTab.getParent();
                if(symTab == null){
                    // variable was not found
                    return null;
                }
            }
        }
        return trace;
    }

    public void enterLoop(Label continueLabel, Label breakLabel){
        loopLabels.push(new LoopLabels(continueLabel, breakLabel));
    }

    public Label continueLabel(){
        return loopLabels.peek().continueLabel();
    }

    public Label breakLabel(){
        return loopLabels.peek().breakLabel();
    }

    public void exitLoop(){
        loopLabels.pop();
    }

    public void withClass(String name, byte[] code){
        classes.put(name, code);
    }

    public Map<String, byte[]> classes(){
        return classes;
    }
}
