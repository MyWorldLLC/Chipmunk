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

import chipmunk.compiler.Symbol;
import chipmunk.compiler.UnresolvedSymbolException;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.ImportNode;
import chipmunk.compiler.ast.ModuleNode;
import chipmunk.compiler.imports.ImportResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ImportResolveVisitor implements AstVisitor {

    protected List<ImportResolver> resolvers;

    public ImportResolveVisitor(){
        this(new ArrayList<>());
    }

    public ImportResolveVisitor(List<ImportResolver> resolvers){
        this.resolvers = resolvers;
    }

    public List<ImportResolver> getResolvers(){
        return resolvers;
    }

    public void setResolvers(List<ImportResolver> resolvers){
        this.resolvers = resolvers;
    }

    @Override
    public void visit(AstNode node) {
        if(node instanceof ModuleNode){
            ModuleNode moduleNode = (ModuleNode) node;

            // Resolve all imported symbols, marking them in the symbol table
            List<ImportNode> imports = moduleNode.getChildren()
                    .stream()
                    .filter(a -> a instanceof ImportNode)
                    .map(a -> (ImportNode) a)
                    .collect(Collectors.toList());

            for(ImportNode i : imports){
                List<Symbol> importSymbols;

                if(i.isImportAll()){
                    importSymbols = getModuleSymbols(i.getModule());
                }else if(i.hasAliases()){
                    importSymbols = new ArrayList<>();

                    List<String> symbols = i.getSymbols();
                    List<String> aliases = i.getAliases();
                    for(int s = 0; s < symbols.size(); s++){

                        Symbol symbol = getModuleSymbol(i.getModule(), symbols.get(s));
                        if(symbol == null){
                            throw new UnresolvedSymbolException(i.getModule(), symbols.get(s));
                        }

                        // TODO - symbol must support aliasing so we can rewrite to access the backing symbol
                        importSymbols.add(new Symbol(aliases.get(s), true));
                    }
                }else{
                    importSymbols = new ArrayList<>();

                    for(String s : i.getSymbols()){

                        Symbol symbol = getModuleSymbol(i.getModule(), s);
                        if(symbol == null){
                            throw new UnresolvedSymbolException(i.getModule(), s);
                        }

                        importSymbols.add(symbol);
                    }
                }

                for(Symbol s : importSymbols){
                    moduleNode.getSymbolTable().setSymbol(s);
                }
            }
        }
    }

    protected List<Symbol> getModuleSymbols(String moduleName){

        for(ImportResolver resolver : resolvers){
            List<Symbol> symbols = resolver.resolveSymbols(moduleName);
            if(symbols != null){
                return symbols;
            }
        }

        return null;
    }

    protected Symbol getModuleSymbol(String moduleName, String name){

        for(ImportResolver resolver : resolvers){
            Symbol symbol = resolver.resolve(moduleName, name);
            if(symbol != null){
                return symbol;
            }
        }

        return null;
    }
}
