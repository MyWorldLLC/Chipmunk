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

package chipmunk.pkg.fs;

import chipmunk.pkg.PackageEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FsPackageEntry implements PackageEntry {

    protected final Path path;

    protected FsPackageEntry(Path path){
        this.path = path;
    }

    @Override
    public String getPath() {
        return path.toString();
    }

    @Override
    public long getSize() throws IOException {
        return Files.size(path);
    }
}
