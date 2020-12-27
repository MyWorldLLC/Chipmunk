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

import java.util.*;
import java.util.stream.Collectors;

public class PackagePath implements Comparable<PackagePath> {

    public static final String PATH_SEPARATOR = "/";

    public static final String SOURCE_EXT = "chp";
    public static final String BIN_EXT = "chpb";

    public static final PackagePath SOURCE_DIR = PackagePath.fromString("src/");
    public static final PackagePath BIN_DIR = PackagePath.fromString("bin/");
    public static final PackagePath RESOURCE_DIR = PackagePath.fromString("resources/");
    public static final PackagePath NATIVE_DIR = PackagePath.fromString("native/");

    protected final boolean directory;
    protected final List<String> parts;
    protected final String pathString;

    public PackagePath(String[] parts){
        this(parts, false);
    }

    public PackagePath(String[] parts, boolean directory){
        this(Arrays.asList(parts), directory);
    }

    private PackagePath(List<String> parts, boolean directory){
        this.parts = Collections.unmodifiableList(parts);
        this.directory = directory;
        pathString = PATH_SEPARATOR + String.join(PATH_SEPARATOR, parts) + (directory ? PATH_SEPARATOR : "");
    }

    public boolean isDirectory(){
        return directory;
    }

    public boolean isFile(){
        return !isDirectory();
    }

    public List<String> getParts(){
        return parts;
    }

    public boolean startsWith(PackagePath other){
        return pathString.startsWith(other.pathString);
    }

    public boolean startsWith(String prefix){
        return pathString.startsWith(prefix);
    }

    public boolean endsWith(PackagePath other){
        return pathString.endsWith(other.pathString);
    }

    public boolean endsWith(String ext){
        return pathString.endsWith(ext);
    }

    public PackagePath join(PackagePath other){
        if(!isDirectory()){
            throw new IllegalStateException("This is not a directory path: " + pathString);
        }

        List<String> newParts = new ArrayList<>(parts.size() + other.parts.size());
        newParts.addAll(parts);
        newParts.addAll(other.parts);

        return new PackagePath(newParts, other.isDirectory());
    }

    public String getName(){
        return parts.get(parts.size() - 1);
    }

    @Override
    public String toString() {
        return pathString;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof PackagePath){
            PackagePath p = (PackagePath) o;
            return parts.equals(p.parts) && directory == p.directory;
        }
        return false;
    }

    @Override
    public int hashCode(){
        return parts.hashCode();
    }

    public static PackagePath fromString(String path){
        path = path.trim();

        boolean directory = path.endsWith(PATH_SEPARATOR);
        String[] parts = path.split(PATH_SEPARATOR);

        return new PackagePath(
                Arrays.stream(parts)
                        .filter(s -> s.length() != 0)
                        .collect(Collectors.toList())
                        .toArray(new String[]{}), directory);
    }

    @Override
    public int compareTo(PackagePath o) {
        return pathString.compareTo(o.pathString);
    }
}
