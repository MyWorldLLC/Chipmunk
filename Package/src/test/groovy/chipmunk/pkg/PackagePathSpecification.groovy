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

package chipmunk.pkg

import spock.lang.Specification

class PackagePathSpecification extends Specification {

	def "/foo is a file"() {
		when:
		def path = PackagePath.fromString("/foo")

		then:
		path.isFile()
		!path.isDirectory()
		path.getParts().size() == 1
		path.toString() == "/foo"
	}

	def "/foo/ is a directory"() {
		when:
		def path = PackagePath.fromString("/foo/")

		then:
		!path.isFile()
		path.isDirectory()
		path.getParts().size() == 1
		path.toString() == "/foo/"
	}
}
