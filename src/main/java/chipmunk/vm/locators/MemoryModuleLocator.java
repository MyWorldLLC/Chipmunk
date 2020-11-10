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

package chipmunk.vm.locators;

import chipmunk.vm.ModuleLocator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryModuleLocator implements ModuleLocator {

    protected final Map<String, byte[]> binaries;

    public MemoryModuleLocator(){
        binaries = new ConcurrentHashMap<>();
    }

    public MemoryModuleLocator(Map<String, byte[]> binaries){
        this();
        this.binaries.putAll(binaries);
    }

    public Map<String, byte[]> getBinaries(){
        return binaries;
    }

    @Override
    public InputStream locate(String moduleName) throws IOException {
        byte[] module = binaries.get(moduleName);
        if(module != null){
            return new ByteArrayInputStream(module);
        }

        return null;
    }
}
