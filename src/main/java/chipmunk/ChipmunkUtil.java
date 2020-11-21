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

package chipmunk;

import chipmunk.binary.BinaryModule;
import chipmunk.compiler.ChipmunkCompiler;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ChipmunkUtil {

    public static List<BinaryModule> compileResources(String... resourcePaths) {
        List<BinaryModule> modules = new ArrayList<>();

        ChipmunkCompiler compiler = new ChipmunkCompiler();
        for(String path : resourcePaths){
            InputStream is = ChipmunkUtil.class.getResourceAsStream(path);
            BinaryModule[] compiled = compiler.compile(is, Paths.get(path).getFileName().toString());
            modules.addAll(Arrays.asList(compiled));
        }

        return modules;
    }

    public static BinaryModule getModule(String moduleName, Collection<BinaryModule> modules){
        BinaryModule namedModule = null;
        for(BinaryModule module : modules){
            if(moduleName.equals(module.getName())){
                namedModule = module;
                break;
            }
        }
        return namedModule;
    }
}
