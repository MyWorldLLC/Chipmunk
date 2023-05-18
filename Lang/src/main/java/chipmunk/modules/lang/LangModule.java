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
import chipmunk.runtime.MethodBinding;
import chipmunk.runtime.UnimplementedMethodException;
import chipmunk.vm.ChipmunkScript;
import chipmunk.vm.invoke.ChipmunkName;
import chipmunk.vm.invoke.security.AllowChipmunkLinkage;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public class LangModule implements ChipmunkModule {

    public static final String MODULE_NAME = "chipmunk.lang";

    @AllowChipmunkLinkage
    @ChipmunkName("Any")
    public final Class<Object> _any;

    @AllowChipmunkLinkage
    @ChipmunkName("Int")
    public final Class<Integer> _int;

    @AllowChipmunkLinkage
    @ChipmunkName("Float")
    public final Class<Float> _float;

    @AllowChipmunkLinkage
    @ChipmunkName("Boolean")
    public final Class<Boolean> _boolean;

    @AllowChipmunkLinkage
    @ChipmunkName("String")
    public final Class<String> _string;

    @AllowChipmunkLinkage
    @ChipmunkName("List")
    public final Class<List> _list;

    @AllowChipmunkLinkage
    @ChipmunkName("Map")
    public final Class<Map> _map;

    public LangModule(){
        _any = Object.class;
        _int = Integer.class;
        _float = Float.class;
        _boolean = Boolean.class;
        _string = String.class;

        _list = List.class;
        _map = Map.class;
    }

    @AllowChipmunkLinkage
    public void unimplementedMethod() throws UnimplementedMethodException {
        throw new UnimplementedMethodException();
    }

    @AllowChipmunkLinkage
    public MethodBinding bindArgs(MethodBinding binding, Integer index, List<Object> args) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return ChipmunkScript.getCurrentScript().getVM().bindArgs(binding, index, args.toArray());
    }

    @Override
    public String getName(){
        return MODULE_NAME;
    }

}
