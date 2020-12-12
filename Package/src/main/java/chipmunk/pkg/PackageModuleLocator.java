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

package chipmunk.pkg;

import chipmunk.vm.ModuleLocator;

import java.io.IOException;
import java.io.InputStream;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

public class PackageModuleLocator implements ModuleLocator {

    protected final PackageReader reader;
    protected final NavigableSet<PackagePath> paths;

    public PackageModuleLocator(PackageReader reader){
        this.reader = reader;
        paths = new TreeSet<>();
    }

    public PackageReader getReader(){
        return reader;
    }

    public void addPath(PackagePath path){
        paths.add(path);
    }

    public boolean hasPath(PackagePath path){
        return paths.contains(path);
    }

    public void removePath(PackagePath path){
        paths.remove(path);
    }

    public NavigableSet<PackagePath> getPaths(){
        return paths;
    }

    @Override
    public InputStream locate(String moduleName) throws IOException {
        moduleName = moduleName + "." + PackagePath.BIN_EXT;
        for(PackagePath path : paths){
            if(path.endsWith(moduleName)){
                return reader.getInputStream(path);
            }
        }
        return null;
    }
}
