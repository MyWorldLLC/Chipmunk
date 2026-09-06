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

import chipmunk.runtime.ChipmunkModule;
import chipmunk.vm.invoke.security.AllowChipmunkLinkage;

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
    public final List<Object> args;

    @AllowChipmunkLinkage
    public final Map<String, Object> env;


    public SystemModule(List<Object> args, Map<String, Object> env, Supplier<String> in, Consumer<Object> out, Consumer<Object> err) {
        if(args == null){
            args = List.of();
        }

        if(env == null){
            env = Map.of();
        }

        this.args = args;
        this.env = env;

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
