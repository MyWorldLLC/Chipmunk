package chipmunk.compiler

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
