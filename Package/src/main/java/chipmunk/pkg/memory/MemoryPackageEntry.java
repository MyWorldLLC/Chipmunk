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

package chipmunk.pkg.memory;

import chipmunk.pkg.PackageEntry;
import chipmunk.pkg.PackagePath;

public class MemoryPackageEntry implements PackageEntry {

    protected final PackagePath path;
    protected final long size;

    protected MemoryPackageEntry(PackagePath path, long size){
        this.path = path;
        this.size = size;
    }

    @Override
    public PackagePath getPath() {
        return path;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public String toString(){
        return "MemoryPackageEntry[" + path.toString() + ":" + size + "]";
    }

}
