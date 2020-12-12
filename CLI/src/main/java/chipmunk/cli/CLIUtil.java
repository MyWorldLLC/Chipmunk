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

package chipmunk.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CLIUtil {

    public static List<Path> collectSources(Path directory) throws IOException {

        List<Path> sources = new ArrayList<>();

        if(!Files.isDirectory(directory)){
            System.err.println("Warning: " + directory + " is not a directory, skipping");
            return Collections.emptyList();
        }

        List<Path> contents = Files.list(directory).collect(Collectors.toList());
        for(Path p : contents){
            if(Files.isDirectory(p)){
                sources.addAll(collectSources(p));
            }else if(p.getFileName().toString().endsWith(ChipmunkCLI.CHIPMUNK_SRC_EXTENSION)){
                sources.add(p.toAbsolutePath().normalize());
            }
        }

        return sources;
    }

    public static List<Path> collectSubDirs(Path directory) throws IOException {
        List<Path> paths = new ArrayList<>();

        if(!Files.isDirectory(directory)){
            System.err.println("Warning: " + directory + " is not a directory, skipping");
            return Collections.emptyList();
        }

        List<Path> contents = Files.list(directory).collect(Collectors.toList());
        for(Path p : contents){
            if(Files.isDirectory(p)){
                paths.addAll(collectSubDirs(p));
            }
        }

        return paths;
    }

}
