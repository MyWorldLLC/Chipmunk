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
import chipmunk.pkg.PackageReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MemoryPackageReader implements PackageReader {

    protected final Map<String, byte[]> entries;

    protected MemoryPackageReader() {
        entries = new HashMap<>();
    }

    public MemoryPackageReader create(InputStream pkgStream) throws IOException {
        MemoryPackageReader reader = new MemoryPackageReader();

        ZipInputStream zipStream = new ZipInputStream(pkgStream);
        while(zipStream.available() == 1){

            ZipEntry zipEntry = zipStream.getNextEntry();

            byte[] data = zipStream.readNBytes((int)zipEntry.getSize());

            entries.put(zipEntry.getName(), data);

            zipStream.closeEntry();
        }

        return reader;
    }

    @Override
    public PackageProperties getPackageProperties() throws IOException {

        byte[] propsData = entries.get(PackageProperties.PACKAGE_FILE);
        if(propsData == null){
            return null;
        }

        PackageProperties props = new PackageProperties();
        props.getProperties().load(new ByteArrayInputStream(propsData));

        return props;
    }

    @Override
    public Collection<PackageEntry> getEntriesIn(String directory) throws IOException {
        return entries.entrySet()
                .stream()
                .filter(e -> e.getKey().startsWith(directory))
                .map(e -> new MemoryPackageEntry(e.getKey(), e.getValue().length))
                .collect(Collectors.toList());
    }

    @Override
    public PackageEntry getEntry(String entryPath) {
        byte[] data = entries.get(entryPath);
        if(data == null){
            return null;
        }

        return new MemoryPackageEntry(entryPath, data.length);
    }

    @Override
    public InputStream getInputStream(PackageEntry entry) {
        byte[] data = entries.get(entry.getPath());
        if(data == null){
            return null;
        }

        return new ByteArrayInputStream(data);
    }

    @Override
    public void close() {}
}