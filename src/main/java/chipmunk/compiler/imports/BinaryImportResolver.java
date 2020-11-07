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

import chipmunk.ModuleLoader;
import chipmunk.binary.*;
import chipmunk.compiler.symbols.Symbol;
import chipmunk.compiler.symbols.SymbolType;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class BinaryImportResolver implements ImportResolver {

    protected ModuleLoader loader;

    public BinaryImportResolver(){
        this(new ModuleLoader());
    }

    public BinaryImportResolver(ModuleLoader loader){
        this.loader = loader;
    }

    public ModuleLoader getModuleLoader() {
        return loader;
    }

    public void setModuleLoader(ModuleLoader loader) {
        this.loader = loader;
    }

    @Override
    public Symbol resolve(String moduleName, String name) {

        try {
            BinaryModule module = loader.load(moduleName);
            if(module == null){
                return null;
            }

            if(module.getNamespace().has(name)){
                BinaryNamespace.Entry e = module.getNamespace().getEntry(name);
                return makeSymbol(e);
            }
        } catch (IOException | BinaryFormatException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public List<Symbol> resolveSymbols(String moduleName) {
        try {
            BinaryModule module = loader.load(moduleName);
            if(module == null){
                return null;
            }

            BinaryNamespace namespace = module.getNamespace();
            return namespace.getEntries()
                    .stream()
                    .map(this::makeSymbol)
                    .collect(Collectors.toList());
        } catch (IOException | BinaryFormatException e) {
            throw new RuntimeException(e);
        }
    }

    protected Symbol makeSymbol(BinaryNamespace.Entry e){
        Symbol symbol = new Symbol(e.getName(), BinaryConstants.isFlagSet(e.getFlags(), BinaryConstants.FINAL_FLAG));

        if(e.getType() == FieldType.DYNAMIC_VAR){
            symbol.setType(SymbolType.VAR);
        }else if(e.getType() == FieldType.CLASS){
            symbol.setType(SymbolType.CLASS);
        }else if(e.getType() == FieldType.METHOD){
            symbol.setType(SymbolType.METHOD);
        }

        return symbol;
    }

}
