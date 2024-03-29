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

package chipmunk

import chipmunk.binary.BinaryMethod
import chipmunk.binary.BinaryModule
import chipmunk.compiler.assembler.ChipmunkAssembler
import chipmunk.vm.ChipmunkVM
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class VMSpecification extends Specification {
	
	ChipmunkVM vm = new ChipmunkVM()
	ChipmunkAssembler assembler = new ChipmunkAssembler()
	
	Integer negOne = Integer.valueOf(-1)
	Integer one = Integer.valueOf(1)
	Integer two = Integer.valueOf(2)
	Integer three = Integer.valueOf(3)

	def "push and pop 1 item"(){
		when:
		stack.push(one)
		
		then:
		stack.pop().getValue() == 1
	}
	
	def "push and pop 2 items"(){
		when:
		stack.push(one)
		stack.push(two)
		
		then:
		stack.pop().getValue() == 2
		stack.pop().getValue() == 1
	}
	
	def "push and dup 1 item"(){
		when:
		stack.push(one)
		stack.dup(0)
		
		then:
		stack.pop().getValue() == 1
		stack.pop().getValue() == 1
	}
	
	def "push 2 items and dup 1 item"(){
		when:
		stack.push(one)
		stack.push(two)
		stack.dup(1)
		
		then:
		stack.pop().getValue() == 1
		stack.pop().getValue() == 2
		stack.pop().getValue() == 1
	}
	
	def "push 2 items and swap them"(){
		when:
		stack.push(one)
		stack.push(two)
		stack.swap(0, 1)
		
		then:
		stack.pop().getValue() == 2
		stack.pop().getValue() == 1
	}
	
	def "push 3 items and swap 2 items"(){
		when:
		stack.push(one)
		stack.push(two)
		stack.push(three)
		stack.swap(0, 2)
		
		then:
		stack.pop().getValue() == 3
		stack.pop().getValue() == 2
		stack.pop().getValue() == 1
	}
	
	def "add"(){
		when:
		assembler.push(two)
		assembler.push(one)
		assembler.add()
		assembler._return()
		def result = vmRun()
		
		then:
		result.getValue() == 3
	}
	
	def "sub"(){
		when:
		assembler.push(two)
		assembler.push(one)
		assembler.sub()
		assembler._return()
		def result = vmRun()
		
		then:
		result.getValue() == -1
	}
	
	def "mul"(){
		when:
		assembler.push(two)
		assembler.push(two)
		assembler.mul()
		assembler._return()
		def result = vmRun()
		
		then:
		result.getValue() == 4
	}
	
	def "div"(){
		when:
		assembler.push(two)
		assembler.push(one)
		assembler.div()
		assembler._return()
		def result = vmRun()
		
		then:
		result.getValue() == 0.5
	}
	
	def "fdiv"(){
		when:
		assembler.push(two)
		assembler.push(one)
		assembler.fdiv()
		assembler._return()
		def result = vmRun()
		
		then:
		result.getValue() == 0
	}
	
	def "mod"(){
		when:
		assembler.push(two)
		assembler.push(three)
		assembler.mod()
		assembler._return()
		def result = vmRun()
		
		then:
		result.getValue() == 1
	}
	
	def "pow"(){
		when:
		assembler.push(three)
		assembler.push(two)
		assembler.pow()
		assembler._return()
		def result = vmRun()
		
		then:
		result.getValue() == 8
	}
	
	def "inc"(){
		when:
		assembler.push(two)
		assembler.inc()
		assembler._return()
		def result = vmRun()
		
		then:
		result.getValue() == 3
	}
	
	def "dec"(){
		when:
		assembler.push(two)
		assembler.dec()
		assembler._return()
		def result = vmRun()
		
		then:
		result.getValue() == 1
	}
	
	def "pos"(){
		when:
		assembler.push(negOne)
		assembler.pos()
		assembler._return()
		def result = vmRun()
		
		then:
		result.getValue() == 1
	}
	
	def "neg"(){
		when:
		assembler.push(two)
		assembler.neg()
		assembler._return()
		def result = vmRun()
		
		then:
		result.getValue() == -2
	}
	
	def "bxor"(){
		when:
		assembler.push(two)
		assembler.push(three)
		assembler.bxor()
		assembler._return()
		def result = vmRun()
		
		then:
		result.getValue() == 1
	}
	
	def "band"(){
		when:
		assembler.push(two)
		assembler.push(three)
		assembler.band()
		assembler._return()
		def result = vmRun()
		
		then:
		result.getValue() == 2
	}
	
	def "bor"(){
		when:
		assembler.push(two)
		assembler.push(three)
		assembler.bor()
		assembler._return()
		def result = vmRun()
		
		then:
		result.getValue() == 3
	}
	
	def "bneg"(){
		when:
		assembler.push(two)
		assembler.bneg()
		assembler._return()
		def result = vmRun()
		
		then:
		result.getValue() == -3
	}
	
	def "lshift"(){
		when:
		assembler.push(one)
		assembler.push(two)
		assembler.lshift()
		assembler._return()
		def result = vmRun()
		
		then:
		result.getValue() == 4
	}
	
	def "rshift"(){
		when:
		assembler.push(one)
		assembler.push(two)
		assembler.rshift()
		assembler._return()
		def result = vmRun()
		
		then:
		result.getValue() == 1
	}
	
	def "urshift"(){
		when:
		assembler.push(one)
		assembler.push(two)
		assembler.urshift()
		assembler._return()
		def result = vmRun()
		
		then:
		result.getValue() == 1
	}
	
	def "push"(){
		when:
		assembler.push(two)
		assembler._return()
		def result = vmRun()
		
		then:
		result.getValue() == 2
	}
	
	def "pop"(){
		when:
		assembler.push(two)
		assembler.push(three)
		assembler.pop()
		assembler._return()
		def result = vmRun()
		
		then:
		result.getValue() == 2
	}

	def "get/setlocal"(){
		when:
		assembler.push(two)
		assembler.setLocal(0)
		assembler.pop()
		assembler.getLocal(0)
		assembler._return()
		def result = vmRun(1)
		
		then:
		result.getValue() == 2
	}
	
	def "callat"(){
		when:
		assembler.push(two)
		assembler.callAt('string', (byte)0)
		assembler._return()
		def result = vmRun()
		
		then:
		result.stringValue() == "2"
	}
	
	def vmRun(int localCount = 0){

		BinaryModule module = new BinaryModule("test")
		module.setConstantPool(assembler.getConstantPool())

		BinaryMethod method = new BinaryMethod()
		method.setModule(module)
		method.setCode(assembler.getCodeSegment())
		method.setLocalCount(localCount)

		return vm.dispatch(method, 0)
	}
}
