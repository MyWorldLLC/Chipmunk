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
import chipmunk.vm.jvm.ForcedYield;
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

    public record EntryPoint(String module, String method){}

    protected final long id;
    private volatile boolean yieldFlag;

    protected EntryPoint entryPoint;

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

    public void entryPoint(EntryPoint entryPoint){
        this.entryPoint = entryPoint;
    }

    public void entryPoint(String module, String method){
        entryPoint(new EntryPoint(module, method));
    }

    public EntryPoint entryPoint(){
        return entryPoint;
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
        ChipmunkScript.setCurrentScript(this);
        if(entryPoint == null){
            entryPoint = new EntryPoint("main", "main");
        }

        ChipmunkModule module = null;

        int state = 0;
        if(fiber.isRewinding()){
            var frame = fiber.rewind();
            state = frame.suspensionPoint;
            module = (ChipmunkModule) frame.locals[0];
            args = (Object[]) frame.locals[1];
        }
        try{
            switch(state){
                case 0:
                    module = loader.instance(entryPoint.module());
                    state = 1;
                case 1:
                    module.initialize(vm);
                    state = 2;
                case 2:
                    return vm.invoke(this, module, entryPoint().method(), args);
            }

        }catch(ForcedYield t){
            var frame = new Fiber.Frame("run", state, 0, 2);
            frame.locals[0] = module;
            frame.locals[1] = args;
            fiber.unwind(frame);
            throw t;
        }
        return null;
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
