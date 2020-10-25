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

package chipmunk;

import chipmunk.runtime.ChipmunkModule;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ChipmunkScript {

    private static final ThreadLocal<ChipmunkScript> currentScript;
    static {
        currentScript = new ThreadLocal<>();
    }

    protected static void setCurrentScript(ChipmunkScript script){
        currentScript.set(script);
    }

    public static ChipmunkScript getCurrentScript(){
        return currentScript.get();
    }

    protected int id;

    protected final List<Object> tags;
    protected final Map<String, ChipmunkModule> loadedModules;

    protected volatile ChipmunkVM vm;
    protected volatile ModuleLoader loader;

    public ChipmunkScript(){
        tags = new CopyOnWriteArrayList<>();
        loadedModules = new ConcurrentHashMap<>();
    }

    public ChipmunkVM getVM() {
        return vm;
    }

    protected void setVM(ChipmunkVM vm) {
        this.vm = vm;
    }

    public void tag(Object tag){
        tags.add(tag);
    }

    public void removeTag(Object tag){
        tags.remove(tag);
    }

    public <T> T getTag(Class<?> tagType){
        for(Object o : tags){
            if(tagType.isInstance(o)){
                return (T) o;
            }
        }
        return null;
    }

    public List<Object> getTags(){
        return tags;
    }

    public int getId(){
        return id;
    }

    protected void setId(int id){
        this.id = id;
    }

    public void setModuleLoader(ModuleLoader loader){
        this.loader = loader;
    }

    public ModuleLoader getModuleLoader(){
        return loader;
    }

    public Map<String, ChipmunkModule> getModulesUnmodifiable() {
        return Collections.unmodifiableMap(loadedModules);
    }

    public void addModule(ChipmunkModule module){
        if(loadedModules.containsKey(module.getName())){
            throw new IllegalStateException(String.format("Module %s is already loaded", module.getName()));
        }

        loadedModules.put(module.getName(), module);
    }

    public boolean isLoaded(String moduleName){
        return loadedModules.containsKey(moduleName);
    }

    public abstract Object run(Object[] args);

    public Object run(){
        return run(null);
    }

}
