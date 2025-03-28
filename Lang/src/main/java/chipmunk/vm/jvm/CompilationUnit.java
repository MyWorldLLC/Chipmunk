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

package chipmunk.vm.jvm;

import chipmunk.vm.ModuleLoader;
import chipmunk.binary.BinaryFormatException;
import chipmunk.binary.BinaryMethod;
import chipmunk.binary.BinaryModule;

import java.io.IOException;

public class CompilationUnit {

    protected ModuleLoader loader;
    protected String entryModule;
    protected String entryMethod;

    protected JvmCompilerConfig jvmCompilerConfig;

    public ModuleLoader getModuleLoader() {
        return loader;
    }

    public void setModuleLoader(ModuleLoader loader) {
        this.loader = loader;
    }

    public String getEntryModule() {
        return entryModule;
    }

    public void setEntryModule(String entryModule) {
        this.entryModule = entryModule;
    }

    public String getEntryMethodName() {
        return entryMethod;
    }

    public void setEntryMethodName(String entryMethod) {
        this.entryMethod = entryMethod;
    }

    /*public BinaryMethod getEntryMethod() throws IOException, BinaryFormatException {
        BinaryModule mainModule = loader.loadChipmunk(entryModule);
        return (BinaryMethod) mainModule.getNamespace().get(entryMethod);
    }*/

    public JvmCompilerConfig getJvmCompilerConfig() {
        return jvmCompilerConfig;
    }

    public void setJvmCompilerConfig(JvmCompilerConfig jvmCompilerConfig) {
        this.jvmCompilerConfig = jvmCompilerConfig;
    }
}
