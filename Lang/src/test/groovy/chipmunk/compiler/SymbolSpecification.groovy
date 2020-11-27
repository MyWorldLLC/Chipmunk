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

import chipmunk.compiler.symbols.Symbol
import spock.lang.Specification

class SymbolSpecification extends Specification {
	
	def "Check symbol default name"(){
		when:
		def symbol = new Symbol()
		
		then:
		symbol.getName() == ""
		symbol.isFinal() == false
		symbol.isShared() == false
	}
	
	def "Check symbol equality"(){
		expect:
		new Symbol("foo") == new Symbol("foo")
		new Symbol("foo") != new Symbol("bar")
	}
	
	def "Check symbol final & shared"(){
		when:
		def symbol1 = new Symbol("foo", false, false)
		def symbol2 = new Symbol("foo2", true)
		def symbol3 = new Symbol("foo3", true, true)
		
		then:
		symbol1.isFinal() == false
		symbol1.isShared() == false
		symbol2.isFinal() == true
		symbol2.isShared() == false
		symbol3.isFinal() == true
		symbol3.isShared() == true
	}

}
