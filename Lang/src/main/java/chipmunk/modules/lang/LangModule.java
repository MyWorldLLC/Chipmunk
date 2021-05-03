/*
 * Copyright (C) 2021 MyWorld, LLC
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

package chipmunk.modules.lang;

import chipmunk.runtime.ChipmunkModule;
import chipmunk.vm.invoke.ChipmunkName;

public class LangModule implements ChipmunkModule {

    public static final String MODULE_NAME = "chipmunk.lang";

    @ChipmunkName("int")
    public final Class<Integer> _int;

    @ChipmunkName("float")
    public final Class<Float> _float;

    @ChipmunkName("boolean")
    public final Class<Boolean> _boolean;

    @ChipmunkName("String")
    public final Class<String> _string;

    public LangModule(){
        _int = Integer.class;
        _float = Float.class;
        _boolean = Boolean.class;
        _string = String.class;
    }

    @Override
    public String getName(){
        return MODULE_NAME;
    }

}
