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

package chipmunk.jvm

import chipmunk.ChipmunkVM
import chipmunk.compiler.ChipmunkCompiler
import spock.lang.Specification

class JvmCompilerSpecification extends Specification {

    ChipmunkVM vm = new ChipmunkVM()
    ChipmunkCompiler cc = new ChipmunkCompiler()
    JvmCompiler jc = new JvmCompiler()

    def "Load as Java code & run"(){
        when:
        def module = cc.compile(getClass().getResourceAsStream("/chipmunk/Map.chp"), "Map.chp")[0]
        def jModIns = jc.compile(module)

        def result = jModIns.main(vm, new Object[0])

        then:
        result == 10
    }
}
