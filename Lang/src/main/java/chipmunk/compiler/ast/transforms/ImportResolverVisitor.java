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

package chipmunk.compiler.ast.transforms;

import chipmunk.compiler.ModuleNotFoundException;
import chipmunk.compiler.UnresolvedSymbolException;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.ImportNode;
import chipmunk.compiler.ast.ModuleNode;
import chipmunk.compiler.imports.ImportResolver;
import chipmunk.compiler.symbols.Symbol;
import chipmunk.compiler.symbols.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class ImportResolverVisitor implements AstVisitor {

    protected List<ImportResolver> resolvers;

    public ImportResolverVisitor(){
        this(new ArrayList<>());
    }

    public ImportResolverVisitor(List<ImportResolver> resolvers){
        this.resolvers = resolvers;
    }

    public List<ImportResolver> getResolvers(){
        return resolvers;
    }

    public void setResolvers(List<ImportResolver> resolvers){
        this.resolvers = resolvers;
    }

    protected SymbolTable currentScope;

    @Override
    public void visit(AstNode node) {
        if(node instanceof ModuleNode){
            currentScope = ((ModuleNode) node).getSymbolTable();
            node.visitChildren(this);
        }else if(node instanceof ImportNode){
            ImportNode importNode = (ImportNode) node;

            List<Symbol> symbols;

            if(importNode.isImportAll()){
                symbols = getModuleSymbols(importNode.getModule());
            }else if(importNode.hasAliases()){
                symbols = new ArrayList<>();

                List<String> importedSymbols = importNode.getSymbols();
                List<String> aliases = importNode.getAliases();
                for(int s = 0; s < importedSymbols.size(); s++){

                    Symbol symbol = getModuleSymbol(importNode.getModule(), importedSymbols.get(s));
                    if(symbol == null){
                        throw new UnresolvedSymbolException(importNode.getModule(), aliases.get(s));
                    }

                    symbol.setName(aliases.get(s));
                    symbol.setImport(new Symbol.Import(importNode.getModule(), importedSymbols.get(s)));
                    symbols.add(symbol);
                }
            }else{
                symbols = new ArrayList<>();

                for(String s : importNode.getSymbols()){

                    Symbol symbol = getModuleSymbol(importNode.getModule(), s);
                    if(symbol == null){
                        throw new UnresolvedSymbolException(importNode.getModule(), s);
                    }

                    symbols.add(symbol);
                }
            }

            if(symbols == null){
                throw new ModuleNotFoundException(importNode.getModule());
            }

            for(Symbol symbol : symbols){
                currentScope.setSymbol(symbol);
            }
        }

    }

    protected List<Symbol> getModuleSymbols(String moduleName){

        for(ImportResolver resolver : resolvers){
            List<Symbol> symbols = resolver.resolveSymbols(moduleName);
            if(symbols != null){
                symbols.forEach(s -> s.setImport(new Symbol.Import(moduleName)));
                return symbols;
            }
        }

        return null;
    }

    protected Symbol getModuleSymbol(String moduleName, String name){

        for(ImportResolver resolver : resolvers){
            Symbol symbol = resolver.resolve(moduleName, name);
            if(symbol != null){
                symbol.setImport(new Symbol.Import(moduleName));
                return symbol;
            }
        }

        return null;
    }
}
