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

import java.util.ArrayDeque;
import java.util.Deque;

public class CompilerState {

    protected SymbolTable scope;

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
}
