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

import chipmunk.vm.hazel.EntryPoint;
import chipmunk.vm.hazel.HazelVM;
import chipmunk.vm.hazel.ScriptResult;
import chipmunk.vm.invoke.LinkingPolicy;
import chipmunk.vm.invoke.SecurityMode;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ChipmunkScript {

    public enum Status {
        RUNNABLE,
        RUNNING,
        EXITED
    }

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

    protected final ChipmunkVM cvm;
    protected final HazelVM vm;
    protected final long id;
    protected final List<Object> tags;
    protected LinkingPolicy linkPolicy;

    protected Consumer<ChipmunkScript> exitHandler;
    protected BiConsumer<ChipmunkScript, Throwable> errorHandler;

    protected final AtomicReference<Status> status = new AtomicReference<>(Status.RUNNABLE);

    public ChipmunkScript(ChipmunkVM cvm, long id, ModuleLoader loader) {
        this(cvm, id, loader, new LinkingPolicy(SecurityMode.DENYING));
    }

    public ChipmunkScript(ChipmunkVM cvm, long id, ModuleLoader loader, LinkingPolicy linkPolicy) {
        this.cvm = cvm;
        this.id = id;
        vm = new HazelVM(loader);
        tags = new CopyOnWriteArrayList<>();

        this.linkPolicy = linkPolicy;
    }

    public ChipmunkVM getVM() {
        return cvm;
    }

    public HazelVM getHazelVM() {
        return vm;
    }

    public Status getStatus() {
        return status.get();
    }

    protected Status setStatus(Status status) {
        return this.status.getAndSet(status);
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

    public long getId(){
        return id;
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

    public ScriptResult run(){
        return vm.run();
    }

    public void yield(){
        vm.yield();
    }

    public void setExitHandler(Consumer<ChipmunkScript> exitHandler){
        this.exitHandler = exitHandler;
    }

    protected Consumer<ChipmunkScript> exitHandler(){
        return exitHandler;
    }

    public void setErrorHandler(BiConsumer<ChipmunkScript, Throwable> errorHandler){
        this.errorHandler = errorHandler;
    }

    protected BiConsumer<ChipmunkScript, Throwable> errorHandler(){
        return errorHandler;
    }

}
