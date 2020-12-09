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

package chipmunk.modules.system;

import chipmunk.runtime.ChipmunkModule;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SystemModule implements ChipmunkModule {

    public static final String SYSTEM_MODULE_NAME = "chipmunk.system";

    public final List<String> args;
    public final Map<String, String> env;

    public SystemModule(String[] args){
        this(args, null);
    }

    public SystemModule(String[] args, Map<String, String> env){
        if(args == null){
            args = new String[]{};
        }
        if(env == null){
            env = Collections.emptyMap();
        }

        this.args = Arrays.asList(args);
        this.env = env;
    }

    public void println(Object msg){
        System.out.println(msg);
    }

    public void print(Object msg){
        System.out.print(msg);
    }

    @Override
    public String getName(){
        return SYSTEM_MODULE_NAME;
    }
}
