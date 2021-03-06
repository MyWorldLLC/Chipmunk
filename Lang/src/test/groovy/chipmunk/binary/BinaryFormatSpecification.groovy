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

package chipmunk.binary

import chipmunk.vm.ChipmunkVM
import chipmunk.compiler.ChipmunkCompiler
import spock.lang.Specification

class BinaryFormatSpecification  extends Specification {

    ChipmunkCompiler compiler = new ChipmunkCompiler()
    ChipmunkVM vm = new ChipmunkVM()
    BinaryWriter writer = new BinaryWriter()
    BinaryReader reader = new BinaryReader()

    def "Write/read binary and run"(){
        when:
        BinaryModule module = compiler.compile(
                getClass()
                        .getResourceAsStream("/chipmunk/binary/BinaryFeatureTest.chp"),
                "BinaryFeatureTest.chp")[0]

        module = writeAndRead(module)
        def script = vm.compileScript(module)
        def result = vm.runAsync(script).get()

        then:
        noExceptionThrown()
        result as String == "TheQuickBrownFox"

    }

    def writeAndRead(BinaryModule module){

        ByteArrayOutputStream os = new ByteArrayOutputStream()
        writer.writeModule(os, module)

        def bytes = os.toByteArray()
        ByteArrayInputStream is = new ByteArrayInputStream(bytes)

        reader.setMaxBufferSize(bytes.length)
        return reader.readModule(is)
    }
}
