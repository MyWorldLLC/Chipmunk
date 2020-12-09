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
import chipmunk.pkg.PackageProperties;
import chipmunk.pkg.PackageWriter;

import java.io.IOException;
import java.io.OutputStream;

public class MemoryPackageWriter implements PackageWriter {

    @Override
    public void writePackageProperties(PackageProperties props) throws IOException {

    }

    @Override
    public OutputStream writeEntry(PackageEntry entry) throws IOException {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
