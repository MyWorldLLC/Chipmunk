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

package chipmunk.vm;

import chipmunk.runtime.ChipmunkModule;
import chipmunk.runtime.Fiber;
import chipmunk.vm.invoke.ChipmunkLibraries;
import chipmunk.vm.invoke.security.LinkingPolicy;
import chipmunk.vm.invoke.security.SecurityMode;
import chipmunk.vm.jvm.JvmCompiler;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChipmunkScript {

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

    protected final long id;
    private volatile boolean yieldFlag;

    protected final List<Object> tags;
    protected final Map<String, ChipmunkModule> modules;
    protected final Fiber fiber;

    protected volatile ChipmunkVM vm;
    protected volatile ModuleLoader loader;
    protected volatile ChipmunkLibraries libs;
    protected volatile LinkingPolicy linkPolicy;
    protected volatile JvmCompiler jvmCompiler;

    public ChipmunkScript(ChipmunkVM vm, long id){
        this.vm = vm;
        this.id = id;
        tags = new CopyOnWriteArrayList<>();
        modules = new ConcurrentHashMap<>();

        linkPolicy = new LinkingPolicy(SecurityMode.ALLOWING);
        fiber = new Fiber();
    }

    public ChipmunkVM vm() {
        return vm;
    }

    public Fiber fiber() {
        return fiber;
    }

    public JvmCompiler getJvmCompiler() {
        return jvmCompiler;
    }

    public void tag(Object tag){
        tags.add(tag);
    }

    public void removeTag(Object tag){
        tags.remove(tag);
    }

    @SuppressWarnings("unchecked")
    public <T> T getTag(Class<T> tagType){
        for(Object o : tags){
            if(tagType.isInstance(o)){
                return (T) o;
            }
        }
        return null;
    }

    public List<Object> tags(){
        return tags;
    }

    public LinkingPolicy linkPolicy(){
        return linkPolicy;
    }

    public void linkPolicy(LinkingPolicy policy){
        linkPolicy = policy;
    }

    public long id(){
        return id;
    }


    public void moduleLoader(ModuleLoader loader){
        this.loader = loader;
    }

    public ModuleLoader moduleLoader(){
        return loader;
    }

    public Map<String, ChipmunkModule> getModulesUnmodifiable() {
        return Collections.unmodifiableMap(modules);
    }

    public void addModule(ChipmunkModule module){
        if(modules.containsKey(module.getName())){
            throw new IllegalStateException(String.format("Module %s is already loaded", module.getName()));
        }

        modules.put(module.getName(), module);
    }

    public boolean isLoaded(String moduleName){
        return modules.containsKey(moduleName);
    }

    public Object run(Object[] args){
        return null; // TODO - formerly abstract
    }

    public Object run(){
        return run(null);
    }

    public void yield(){
        yieldFlag = true;
    }

    public boolean isYielded(){
        return yieldFlag;
    }

    public void resume(){
        yieldFlag = false;
    }

    public void libs(ChipmunkLibraries libs){
        this.libs = libs;
    }

    public ChipmunkLibraries libs(){
        return libs;
    }

}
