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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FileModuleLocator implements ModuleLocator {

    public static final String BINARY_FILE_EXTENSION = ".chpb";

    protected final CopyOnWriteArrayList<Path> paths;

    public FileModuleLocator(){
        paths = new CopyOnWriteArrayList<>();
    }

    public FileModuleLocator(List<Path> paths){
        this();
        this.paths.addAll(paths);
    }

    public void addPath(Path path){
        paths.addIfAbsent(path);
    }

    public void removePath(Path path){
        paths.remove(path);
    }

    public List<Path> getPaths(){
        return paths;
    }

    @Override
    public InputStream locate(String moduleName) throws IOException {

        if(!moduleName.endsWith(BINARY_FILE_EXTENSION)){
            moduleName = moduleName + BINARY_FILE_EXTENSION;
        }

        for(Path dirPath : paths){
            Path path = dirPath.resolve(moduleName);
            if(Files.exists(path)){
                return Files.newInputStream(path);
            }
        }
        return null;
    }

}
