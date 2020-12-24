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
import chipmunk.pkg.PackageProperties;
import chipmunk.pkg.PackageWriter;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class FsPackageWriter implements PackageWriter {

    protected final FileSystem fs;

    protected FsPackageWriter(FileSystem fs){
        if(fs == null){
            fs = FileSystems.getDefault();
        }
        this.fs = fs;
    }

    public static FsPackageWriter create(FileSystem fs) {
        return new FsPackageWriter(fs);
    }

    public static FsPackageWriter create(Path filePath) throws IOException {
        return new FsPackageWriter(FileSystems.newFileSystem(filePath));
    }

    @Override
    public void writePackageProperties(PackageProperties props) throws IOException {
        Path propsPath = fs.getPath(PackageProperties.PACKAGE_FILE);

        OutputStream os = new BufferedOutputStream(Files.newOutputStream(propsPath));
        props.getProperties().store(os, null);
        os.close();
    }

    @Override
    public OutputStream writeEntry(PackageEntry entry) throws IOException {
        Path entryPath = fs.getPath(entry.getPath().toString());
        return Files.newOutputStream(entryPath);
    }

    @Override
    public void close() throws IOException {
        fs.close();
    }
}
