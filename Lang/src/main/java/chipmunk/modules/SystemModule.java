/*
 * Copyright (C) 2026 MyWorld, LLC
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

package chipmunk.modules;

import chipmunk.runtime.CList;
import chipmunk.runtime.CMap;
import chipmunk.runtime.ChipmunkModule;
import chipmunk.vm.ChipmunkScript;
import chipmunk.vm.invoke.AllowChipmunkLinkage;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SystemModule implements ChipmunkModule {

    public static final String SYSTEM_MODULE_NAME = "chipmunk.system";

    protected final Supplier<String> in;
    protected final Consumer<Object> out;
    protected final Consumer<Object> err;

    @AllowChipmunkLinkage
    public final CList args;

    @AllowChipmunkLinkage
    public final CMap env;


    public SystemModule(ChipmunkScript script, List<Object> args, Map<String, Object> env, Supplier<String> in, Consumer<Object> out, Consumer<Object> err) {
        if(args == null){
            args = List.of();
        }

        if(env == null){
            env = Map.of();
        }

        if(script != null){
            // Script will be null if this is instantiated during compilation.
            var vm = script.getHazelVM();

            this.args = new CList(vm, args.size());
            for(var arg : args){
                var argPtr = vm.fromHostValue(arg);
                this.args.add(argPtr);
            }

            this.env = new CMap(vm, env.size());
            for(var envKey : env.keySet()){
                this.env.insert(vm.fromHostValue(envKey), vm.fromHostValue(env.get(envKey)));
            }
        }else{
            this.args = null;
            this.env = null;
        }


        this.in = in;
        this.out = out;
        this.err = err;
    }

    @AllowChipmunkLinkage
    public void println(Object msg){
        out.accept(msg);
    }

    @AllowChipmunkLinkage
    public void printErr(Object msg){
        err.accept(msg);
    }

    @AllowChipmunkLinkage
    public String readIn(){
        return in.get();
    }

    @Override
    public String getName(){
        return SYSTEM_MODULE_NAME;
    }

}
