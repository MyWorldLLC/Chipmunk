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
import chipmunk.vm.hazel.EntryPoint;
import chipmunk.vm.hazel.HazelVM;
import chipmunk.vm.invoke.ChipmunkLibraries;
import chipmunk.vm.invoke.security.LinkingPolicy;
import chipmunk.vm.invoke.security.SecurityMode;
import chipmunk.vm.jvm.JvmCompiler;
import chipmunk.vm.jvm.JvmCompilerConfig;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public static void trap(Object payload){
        var handler = getCurrentScript().getTrapHandler();
        if(handler != null){
            handler.runtimeTrap(payload);
        }
    }

    protected final ChipmunkVM cvm;
    protected final long id;
    protected final HazelVM vm;

    protected final List<Object> tags;

    protected TrapHandler trapHandler;
    protected ChipmunkLibraries libs;
    protected LinkingPolicy linkPolicy;

    public ChipmunkScript(ChipmunkVM cvm, long id, ModuleLoader loader){
        this(cvm, id, loader, null);
    }

    public ChipmunkScript(ChipmunkVM cvm, long id, ModuleLoader loader, TrapHandler trapHandler) {
        this.cvm = cvm;
        this.id = id;
        this.trapHandler = trapHandler;
        vm = new HazelVM(loader);
        tags = new CopyOnWriteArrayList<>();

        linkPolicy = new LinkingPolicy(SecurityMode.ALLOWING);
    }

    public ChipmunkVM getVM() {
        return cvm;
    }

    public HazelVM getHazelVM() {
        return vm;
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

    public List<Object> getTags(){
        return tags;
    }

    public LinkingPolicy getLinkPolicy(){
        return linkPolicy;
    }

    public void setLinkPolicy(LinkingPolicy policy){
        linkPolicy = policy;
    }

    public long getId(){
        return id;
    }

    public void setTrapHandler(TrapHandler trapHandler){
        this.trapHandler = trapHandler;
    }

    public TrapHandler getTrapHandler(){
        return trapHandler != null ? trapHandler : cvm.getDefaultTrapHandler();
    }

    public ModuleLoader getModuleLoader(){
        return vm.moduleLoader();
    }

    public EntryPoint entryPoint(){
        return vm.entryPoint();
    }

    public void setEntryPoint(EntryPoint entryPoint){
        vm.entryPoint(entryPoint);
    }

    public Optional<Object> run(){
        return vm.run();
    }

    public void yield(){
        vm.yield();
    }

    public boolean isYielded(){
        return vm.isYieldRequested();
    }

    public void setLibs(ChipmunkLibraries libs){
        this.libs = libs;
    }

    public ChipmunkLibraries getLibs(){
        return libs;
    }

}
