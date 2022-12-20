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
import chipmunk.compiler.ast.*;
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
        }else if(node.is(NodeType.IMPORT)){

            List<Symbol> symbols;

            if(Imports.isImportAll(node)){
                symbols = getModuleSymbols(Imports.getModule(node).getName());
            }else if(Imports.isAliased(node)){
                symbols = new ArrayList<>();

                List<Symbol> importedSymbols = Imports.symbols(node);
                List<Symbol> aliases = Imports.aliases(node);

                for(int s = 0; s < importedSymbols.size(); s++){

                    Symbol symbol = getModuleSymbol(Imports.getModule(node), importedSymbols.get(s));
                    if(symbol == null){
                        throw new UnresolvedSymbolException(Imports.getModule(node).getName(), aliases.get(s).getName());
                    }

                    symbol.setName(aliases.get(s).getName());
                    symbol.setImport(new Symbol.Import(Imports.getModule(node).getName(), importedSymbols.get(s).getName()));
                    symbols.add(symbol);
                }
            }else{
                symbols = new ArrayList<>();

                for(Symbol s : Imports.symbols(node)){

                    Symbol symbol = getModuleSymbol(Imports.getModule(node), s);
                    if(symbol == null){
                        throw new UnresolvedSymbolException(Imports.getModule(node).getName(), s.getName());
                    }

                    symbols.add(symbol);
                }
            }

            if(symbols == null){
                throw new ModuleNotFoundException(Imports.getModule(node).getName());
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

    protected Symbol getModuleSymbol(Symbol moduleName, Symbol name){

        for(ImportResolver resolver : resolvers){
            Symbol symbol = resolver.resolve(moduleName.getName(), name.getName());
            if(symbol != null){
                symbol.setImport(new Symbol.Import(moduleName.getName()));
                return symbol;
            }
        }

        return null;
    }
}
