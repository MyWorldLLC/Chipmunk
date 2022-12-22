/*
 * Copyright (C) 2020 MyWorld, LLC
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

package chipmunk.compiler.imports;

import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.symbols.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AstImportResolver implements ImportResolver {

    protected List<AstNode> modules;

    public AstImportResolver(){
        modules = new ArrayList<>();
    }

    public AstImportResolver(List<AstNode> modules){
        this.modules = modules;
    }

    public List<AstNode> getModules() {
        return modules;
    }

    public void setModules(List<AstNode> modules) {
        this.modules = modules;
    }

    @Override
    public Symbol resolve(String moduleName, String name) {

        Optional<AstNode> node = getModuleNode(moduleName);

        if(node.isEmpty()){
            return null;
        }

        Symbol symbol = node.get().getSymbolTable().getSymbol(name);
        if(symbol == null || symbol.isImported()){
            return null;
        }

        return symbol.clone();
    }

    @Override
    public List<Symbol> resolveSymbols(String moduleName) {

        Optional<AstNode> node = getModuleNode(moduleName);
        if(node.isEmpty()){
            return null;
        }

        return node.get().getSymbolTable()
                .getSymbolsUnmodifiable()
                .stream()
                .filter(s -> !s.isImported())
                .map(Symbol::clone)
                .collect(Collectors.toList());
    }

    public Optional<AstNode> getModuleNode(String moduleName){
        return modules.stream()
                .filter(n -> n.getSymbol().getName().equals(moduleName))
                .findFirst();
    }
}
