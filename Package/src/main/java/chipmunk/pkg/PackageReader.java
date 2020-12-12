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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public interface PackageReader extends Closeable {

    PackageProperties getPackageProperties() throws IOException;

    default Collection<PackageEntry> getEntries() throws IOException {
        return getEntriesIn(PackagePath.fromString("/"));
    }

    Collection<PackageEntry> getEntriesIn(PackagePath directory) throws IOException;

    PackageEntry getEntry(PackagePath path) throws IOException;

    InputStream getInputStream(PackageEntry entry) throws IOException;

    default InputStream getInputStream(PackagePath path) throws IOException {
        return getInputStream(getEntry(path));
    }

}
