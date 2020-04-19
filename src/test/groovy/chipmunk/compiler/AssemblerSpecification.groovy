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

package chipmunk.compiler

import chipmunk.ChipmunkVM
import chipmunk.modules.runtime.CInteger
import chipmunk.modules.runtime.CMethod
import spock.lang.Specification

class AssemblerSpecification extends Specification {
	
	ChipmunkVM context = new ChipmunkVM()
	ChipmunkAssembler assembler = new ChipmunkAssembler()

	def "Assemble and run 1 + 2"(){
		when:
		assembler.push(new CInteger(1))
		assembler.push(new CInteger(2))
		assembler.add()
		assembler._return()
		
		def result = callMethod()
		
		then:
		result instanceof CInteger
		result.getValue() == 3
	}
	
	def callMethod(){
		
		CMethod method = new CMethod()
		method.setInstructions(assembler.getCodeSegment())
		method.setConstantPool(assembler.getConstantPool().toArray())
		method.setLocalCount(0)
		
		return context.dispatch(method, 0)
	}
}
