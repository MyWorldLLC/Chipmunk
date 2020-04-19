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

package chipmunk.modules;

import chipmunk.ChipmunkUtil;
import chipmunk.modules.buffer.BufferCClass;
import chipmunk.modules.math.CMath;
import chipmunk.modules.runtime.CFloat;
import chipmunk.modules.runtime.CModule;
import chipmunk.modules.uuid.UUIDSupport;

import java.util.Collection;

public class ChipmunkModuleBuilder {

    public static CModule buildLangModule(){
        CModule lang = new CModule("chipmunk.lang");

        return lang;
    }

    public static CModule buildBufferModule() {
        CModule module = new CModule("chipmunk.buffer");
        module.getNamespace().set("Buffer", new BufferCClass(module));
        return module;
    }

    public static CModule buildUUIDModule() {
        Collection<CModule> modules = ChipmunkUtil.compileResources("/chipmunk/modules/chipmunk.uuid.chp");
        CModule uuidModule = ChipmunkUtil.getModule("chipmunk.uuid", modules);

        uuidModule.getNamespace().set("_randomUUID", UUIDSupport.createRandomUUID());
        uuidModule.getNamespace().set("_fromString", UUIDSupport.uuidFromString());
        uuidModule.getNamespace().set("_toString", UUIDSupport.uuidToString());

        return uuidModule;
    }

    public static CModule buildMathModule(){
        CModule math = new CModule("chipmunk.math");

        math.getNamespace().set("E", new CFloat((float)Math.E));
        math.getNamespace().set("PI", new CFloat((float)Math.PI));

        math.getNamespace().set("Math", new CMath());

        return math;
    }
}
