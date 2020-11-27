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

import chipmunk.compiler.symbols.Symbol;
import chipmunk.compiler.symbols.SymbolType;
import chipmunk.runtime.ChipmunkModule;
import chipmunk.vm.ModuleLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class NativeImportResolver implements ImportResolver {

    protected ModuleLoader loader;

    public NativeImportResolver(){
        this(new ModuleLoader());
    }

    public NativeImportResolver(ModuleLoader loader){
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

        ChipmunkModule module = loader.loadNative(moduleName);
        if(module == null){
            return null;
        }

        Field[] fields = module.getClass().getFields();
        Field f = find(fields, name);
        if (f != null) {
            return makeFieldSymbol(f);
        }

        Method[] methods = module.getClass().getMethods();
        Method m = find(methods, name);
        if (m != null) {
            return makeMethodSymbol(m);
        }

        return null;
    }

    @Override
    public List<Symbol> resolveSymbols(String moduleName) {

        ChipmunkModule module = loader.loadNative(moduleName);
        if(module == null){
            return null;
        }

        List<Symbol> symbols = new ArrayList<>();

        Field[] fields = module.getClass().getFields();
        for(Field f : fields){
            symbols.add(makeFieldSymbol(f));
        }

        Method[] methods = module.getClass().getMethods();
        for(Method m : methods){
            symbols.add(makeMethodSymbol(m));
        }

        return null;
    }

    protected Symbol makeFieldSymbol(Field f){
        Symbol s = new Symbol(f.getName(), Modifier.isFinal(f.getModifiers()));
        s.setType(SymbolType.VAR);
        return s;
    }

    protected Symbol makeMethodSymbol(Method m){
        Symbol s = new Symbol(m.getName(), Modifier.isFinal(m.getModifiers()));
        s.setType(SymbolType.METHOD);
        return s;
    }

    protected Field find(Field[] fields, String name){
        for(Field f : fields){
            if(f.getName().equals(name)){
                return f;
            }
        }
        return null;
    }

    protected Method find(Method[] methods, String name){
        for(Method m : methods){
            if(m.getName().equals(name)){
                return m;
            }
        }
        return null;
    }

}
