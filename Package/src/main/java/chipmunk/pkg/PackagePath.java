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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PackagePath {

    public static final String PATH_SEPARATOR = "/";

    public static final String SOURCE_DIR = "src";
    public static final String BIN_DIR = "bin";
    public static final String RESOURCE_DIR = "resources";
    public static final String NATIVE_DIR = "native";

    protected List<String> parts;

    public PackagePath(){
        parts = new ArrayList<>();
    }

    public PackagePath(String[] parts){
        this();
        this.parts.addAll(Arrays.asList(parts));
    }

    public PackagePath(List<String> parts){
        Objects.requireNonNull(parts);
        this.parts = parts;
    }

    @Override
    public String toString() {
        return PATH_SEPARATOR + String.join(PATH_SEPARATOR, parts);
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof PackagePath){
            PackagePath p = (PackagePath) o;
            return parts.equals(p.parts);
        }
        return false;
    }

    @Override
    public int hashCode(){
        return parts.hashCode();
    }

    public PackagePath fromString(String path){
        path = path.trim();

        String[] parts = path.split(PATH_SEPARATOR);
        return new PackagePath(
                Arrays.stream(parts)
                        .filter(s -> s.length() != 0)
                        .collect(Collectors.toList()));
    }
}
