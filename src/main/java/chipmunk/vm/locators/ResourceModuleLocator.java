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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ResourceModuleLocator implements ModuleLocator {

    protected final CopyOnWriteArrayList<String> resourcePaths;

    public ResourceModuleLocator(){
        resourcePaths = new CopyOnWriteArrayList<>();
    }

    public ResourceModuleLocator(List<String> paths){
        this();
        resourcePaths.addAll(paths);
    }

    public void addPath(String path){
        resourcePaths.addIfAbsent(path);
    }

    public void removePath(String path){
        resourcePaths.remove(path);
    }

    public List<String> getPaths(){
        return resourcePaths;
    }

    @Override
    public InputStream locate(String moduleName) throws IOException {
        for(String path : resourcePaths){

            if(!path.endsWith("/")){
                path = path + "/";
            }

            InputStream is = getClass().getResourceAsStream(path + moduleName);
            if(is != null){
                return is;
            }
        }
        return null;
    }
}
