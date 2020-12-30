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

package chipmunk.pkg.memory

import chipmunk.pkg.PackageEntry
import chipmunk.pkg.PackagePath
import spock.lang.Specification

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class MemoryPackageSpecification extends Specification {

    def "Single entry package saves and loads correctly"() {
        when:
        def os = new ByteArrayOutputStream()
        MemoryPackageWriter writer = MemoryPackageWriter.create(os)

        writer.write(PackagePath.fromString("/foo"), "Hello, World!".getBytes(StandardCharsets.UTF_8))
        writer.close()

        MemoryPackageReader reader = MemoryPackageReader.create(new ByteArrayInputStream(os.toByteArray()))
        PackageEntry entry = reader.getEntry(PackagePath.fromString("/foo"))
        byte[] data = reader.getInputStream(entry).readAllBytes()

        then:
        data != null
        new String(data, StandardCharsets.UTF_8) == "Hello, World!"
    }

    def "Scanning nested entries returns all"() {
        when:
        def os = new ByteArrayOutputStream()
        MemoryPackageWriter writer = MemoryPackageWriter.create(os)

        writer.write(PackagePath.fromString("/foo/a"), "Hello".getBytes(StandardCharsets.UTF_8))
        writer.write(PackagePath.fromString("/foo/b"), "World".getBytes(StandardCharsets.UTF_8))
        writer.close()

        MemoryPackageReader reader = MemoryPackageReader.create(new ByteArrayInputStream(os.toByteArray()))
        def entries = reader.getEntriesIn(PackagePath.fromString("/fo"))

        then:
        entries.size() == 2
        entries[0].getPath().toString() == "/foo/a"
        entries[1].getPath().toString() == "/foo/b"

    }
}
