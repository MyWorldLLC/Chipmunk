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

import chipmunk.compiler.ChipmunkSource;
import chipmunk.vm.ModuleLocator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PackageLoader {

    protected final PackageReader reader;

    public PackageLoader(PackageReader reader){
        this.reader = reader;
    }

    public PackageReader getReader(){
        return reader;
    }

    public PackageProperties getPackageProperties() throws IOException {
        return reader.getPackageProperties();
    }

    public List<ChipmunkSource> getSources() throws IOException {
        Collection<PackageEntry> packageEntries = reader.getEntriesIn(PackagePath.SOURCE_DIR);

        List<ChipmunkSource> sources = new ArrayList<>();
        for(PackageEntry entry : packageEntries){
            if(entry.getPath().endsWith(PackagePath.SOURCE_EXT)){
                sources.add(new ChipmunkSource(reader.getInputStream(entry), entry.getPath().toString()));
            }
        }

        return sources;
    }

    public ModuleLocator getBinaryLocator() throws IOException {
        Collection<PackageEntry> packageEntries = reader.getEntriesIn(PackagePath.BIN_DIR);

        PackageModuleLocator locator = new PackageModuleLocator(reader);
        for(PackageEntry entry : packageEntries){
            if(entry.getPath().endsWith(PackagePath.BIN_EXT)){
                locator.addPath(entry.getPath());
            }
        }

        return locator;
    }

    public Collection<PackageEntry> getResources() throws IOException {
        return reader.getEntriesIn(PackagePath.RESOURCE_DIR);
    }

}
