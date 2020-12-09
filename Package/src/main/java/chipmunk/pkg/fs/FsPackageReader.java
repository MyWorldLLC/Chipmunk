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
import chipmunk.pkg.PackageReader;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class FsPackageReader implements PackageReader {

    protected final FileSystem fs;

    protected FsPackageReader(FileSystem fs){
        if(fs == null){
            fs = FileSystems.getDefault();
        }
        this.fs = fs;
    }

    public FsPackageReader create(FileSystem fs) {
        return new FsPackageReader(fs);
    }

    public FsPackageReader create(Path filePath) throws IOException {
        return new FsPackageReader(FileSystems.newFileSystem(filePath));
    }

    @Override
    public PackageProperties getPackageProperties() throws IOException {

        Path propsPath = fs.getPath(PackageProperties.PACKAGE_FILE);

        if(!Files.exists(propsPath)){
            throw new FileNotFoundException("Missing " + PackageProperties.PACKAGE_FILE);
        }

        InputStream is = new BufferedInputStream(Files.newInputStream(propsPath));

        Properties properties = new Properties();
        properties.load(is);

        is.close();

        return new PackageProperties(properties);
    }

    @Override
    public Collection<PackageEntry> getEntriesIn(String directory) throws IOException {

        List<PackageEntry> entries = new ArrayList<>();
        for(Path p : Files.newDirectoryStream(fs.getPath(directory))){
            if(!Files.isDirectory(p)){
                entries.add(new FsPackageEntry(p));
            }
        }

        return entries;
    }

    @Override
    public PackageEntry getEntry(String entryPath) {
        return new FsPackageEntry(fs.getPath(entryPath));
    }

    @Override
    public InputStream getInputStream(PackageEntry entry) throws IOException {
        return Files.newInputStream(fs.getPath(entry.getPath()));
    }

    @Override
    public void close() throws IOException {
        fs.close();
    }
}
