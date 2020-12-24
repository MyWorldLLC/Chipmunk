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
import chipmunk.pkg.PackageProperties;
import chipmunk.pkg.PackageWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MemoryPackageWriter implements PackageWriter {

    protected ZipOutputStream zip;

    protected MemoryPackageWriter(OutputStream os) {
        zip = new ZipOutputStream(os);
    }

    public static MemoryPackageWriter create(OutputStream os) {
        return new MemoryPackageWriter(os);
    }

    @Override
    public void writePackageProperties(PackageProperties props) throws IOException {
        PackagePath path = PackagePath.fromString(PackageProperties.PACKAGE_FILE);
        ZipEntry entry = new ZipEntry(path.toString());
        zip.putNextEntry(entry);
        props.getProperties().store(zip, null);
        zip.closeEntry();
    }

    @Override
    public ZipOutputStream writeEntry(PackageEntry entry) throws IOException {

        ZipEntry zipEntry = new ZipEntry(entry.getPath().toString());
        zip.putNextEntry(zipEntry);

        return zip;
    }

    @Override
    public void close() throws IOException {
        zip.flush();
        zip.close();
    }
}
