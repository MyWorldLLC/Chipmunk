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

import chipmunk.binary.BinaryFormatException;
import chipmunk.binary.BinaryModule;
import chipmunk.binary.FieldType;
import chipmunk.runtime.CClass;
import chipmunk.runtime.CMethod;
import chipmunk.runtime.CModule;
import chipmunk.runtime.ChipmunkModule;
import chipmunk.vm.invoke.ChipmunkLibraries;
import chipmunk.vm.invoke.security.LinkingPolicy;
import chipmunk.vm.invoke.security.SecurityMode;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

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

    protected long id;

    protected final List<Object> tags;
    protected final Map<String, ChipmunkModule> modules;

    protected final ChipmunkVM vm;
    protected final List<Fiber> fibers;
    protected final AtomicReference<Fiber> currentFiber;
    protected final Deque<Fiber> runQueue;

    protected final ModuleLoader loader;
    protected ChipmunkLibraries libs;
    protected LinkingPolicy linkPolicy;

    protected EntryPoint entryPoint;

    public ChipmunkScript(ChipmunkVM vm){
        this(vm, new EntryPoint("main", "main"));
    }

    public ChipmunkScript(ChipmunkVM vm, EntryPoint entryPoint){
        this.vm = vm;
        this.entryPoint = entryPoint;
        fibers = new ArrayList<>();
        currentFiber = new AtomicReference<>();
        runQueue = new ArrayDeque<>();
        tags = new CopyOnWriteArrayList<>();
        modules = new ConcurrentHashMap<>();

        this.loader = new ModuleLoader();

        linkPolicy = new LinkingPolicy(SecurityMode.ALLOWING);
    }

    public ChipmunkVM getVM() {
        return vm;
    }

    public EntryPoint getEntryPoint() {
        return entryPoint;
    }

    public void setEntryPoint(EntryPoint entryPoint){
        this.entryPoint = entryPoint;
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

    protected void setId(long id){
        this.id = id;
    }

    public ModuleLoader getModuleLoader(){
        return loader;
    }

    public Fiber getCurrentFiber(){
        return currentFiber.get();
    }

    public CMethod getEntryMethod(){
        return getMethod(entryPoint.getModule(), entryPoint.getMethod());
    }

    public CMethod getMethod(String moduleName, String method){
        var module = getCModule(moduleName);
        return module.cls.getInstanceMethod(method);
    }

    public CModule getCModule(String name) throws ModuleLoadException {

        if(modules.containsValue(name)){
            return (CModule) modules.get(name);
        }

        BinaryModule binary;
        try {
            binary = loader.loadBinary(name);
        } catch (IOException | BinaryFormatException e) {
            throw new ModuleLoadException(e);
        }

        var module = new CModule(name);
        module.constants = binary.getConstantPool();
        module.setFileName(binary.getFileName());

        var builder = CClass.builder(name);
        for(var entry : binary.getNamespace()){
            if(entry.getType() == FieldType.METHOD){
                // TODO - API is horrible and we need to add things like the exception and debug tables
                var bm = entry.getBinaryMethod();
                var cm = new CMethod(module, entry.getName(), bm.getArgCount());
                cm.localCount = bm.getLocalCount();
                cm.code = bm.getCode();

                builder.withInstanceMethod(cm);
            }// TODO - all the other things
        }

        module.cls = builder.build();
        modules.put(module.getName(), module);

        return module;
    }

    public void initEntryFiber(Object... args){
        var module = getCModule(entryPoint.getModule());
        var fiber = newFiber();
        fiber.initialize(module.cls.getInstanceMethod(entryPoint.getMethod()), args);
        setCurrentFiber(fiber);
    }

    // TODO - run from suspended state

    public Object run(Object... args){

        try {
            getCModule(entryPoint.getModule());

            var fiber = newFiber();
            setCurrentFiber(fiber);
            fiber.initialize(getEntryMethod(), args);
            BytecodeInterpreter.run(fiber);
            return fiber.pop();

        } catch (Throwable e) {
            throw e;
        }

    }

    public Fiber newFiber(){
        var fiber = new Fiber(this);
        fibers.add(fiber);
        return fiber;
    }

    public void setCurrentFiber(Fiber f){
        currentFiber.set(f);
    }

    public void interrupt(){
        currentFiber.getAndUpdate(f -> {
            if(f != null){
                f.interrupt();
            }
            return f;
        });
    }

    public void setLibs(ChipmunkLibraries libs){
        this.libs = libs;
    }

    public ChipmunkLibraries getLibs(){
        return libs;
    }

}
