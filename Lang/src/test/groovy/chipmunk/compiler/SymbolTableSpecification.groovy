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
import chipmunk.compiler.symbols.SymbolTable
import spock.lang.Specification

class SymbolTableSpecification extends Specification {

	def "Empty local table"(){
		when:
        SymbolTable symTab = new SymbolTable(SymbolTable.Scope.LOCAL)
		
		then:
		symTab.getScope() == SymbolTable.Scope.LOCAL
		symTab.getSymbolCount() == 0
		symTab.getLocalMax() == 0
	}
	
	def "Nested local scopes"(){
		when:
		SymbolTable symTab1 = new SymbolTable(SymbolTable.Scope.METHOD)
		SymbolTable symTab2 = new SymbolTable(SymbolTable.Scope.LOCAL)
		SymbolTable symTab3 = new SymbolTable(SymbolTable.Scope.LOCAL)
		SymbolTable symTab4 = new SymbolTable(SymbolTable.Scope.LOCAL)
		
		symTab1.setSymbol(new Symbol("foo1"))
		symTab2.setSymbol(new Symbol("foo2"))
		symTab3.setSymbol(new Symbol("foo3"))
		
		symTab4.setSymbol(new Symbol("foo4"))
		
		symTab2.setParent(symTab1)
		symTab3.setParent(symTab2)
		symTab4.setParent(symTab2)
		
		then:
		symTab1.getLocalMax() == 3
		symTab2.getLocalMax() == 2
		symTab3.getLocalMax() == 1
		symTab4.getLocalMax() == 1
		
		symTab4.getLocalIndex(symTab4.getSymbol("foo4")) == 2
		symTab3.getLocalIndex(symTab3.getSymbol("foo3")) == 2
		symTab2.getLocalIndex(symTab2.getSymbol("foo2")) == 1
		symTab1.getLocalIndex(symTab1.getSymbol("foo1")) == 0
	}
}
