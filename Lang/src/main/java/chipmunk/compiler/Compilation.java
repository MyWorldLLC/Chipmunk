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

package chipmunk.compiler;

import chipmunk.vm.ModuleLoader;

import java.util.ArrayList;
import java.util.List;

public class Compilation {

    protected final List<ChipmunkSource> sources;
    protected final CompilerConfig compilerConfig;
    protected final ModuleLoader moduleLoader;

    public Compilation(){
        this(CompilerConfig.DEFAULT, new ModuleLoader());
    }

    public Compilation(CompilerConfig compilerConfig){
        this(compilerConfig, new ModuleLoader());
    }

    public Compilation(CompilerConfig compilerConfig, ModuleLoader moduleLoader){
        sources = new ArrayList<>();
        this.compilerConfig = compilerConfig;
        this.moduleLoader = moduleLoader;
    }

    public List<ChipmunkSource> getSources(){
        return sources;
    }

    public void addSource(ChipmunkSource source){
        sources.add(source);
    }

    public CompilerConfig getCompilerConfig() {
        return compilerConfig;
    }

    public ModuleLoader getModuleLoader() {
        return moduleLoader;
    }
}
