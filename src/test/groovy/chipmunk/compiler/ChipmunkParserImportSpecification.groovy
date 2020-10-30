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

import chipmunk.compiler.ast.ImportNode
import chipmunk.compiler.lexer.ChipmunkLexer
import chipmunk.compiler.parser.ChipmunkParser
import spock.lang.Specification

class ChipmunkParserImportSpecification extends Specification {

	def "parse import foobar"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("import foobar")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		ImportNode node = parser.parseImport()
		
		then:
		node.toString() == "(import foobar)"
	}
	
	def "parse import foo.bar"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("import foo.bar")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		ImportNode node = parser.parseImport()
		
		then:
		node.toString() == "(import bar from foo)"
	}
	
	def "parse import foo.bar.baz"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("import foo.bar.baz")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		ImportNode node = parser.parseImport()
		
		then:
		node.toString() == "(import baz from foo.bar)"
	}
	
	def "parse import foo.bar.*"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("import foo.bar.*")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		ImportNode node = parser.parseImport()
		
		then:
		node.toString() == "(import * from foo.bar)"
	}
	
	def "parse import foo.bar as baz"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("import foo.bar as baz")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		ImportNode node = parser.parseImport()
		
		then:
		node.toString() == "(import bar as baz from foo)"
	}
	
	def "parse import foo.bar.* as baz"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("import foo.bar.* as baz")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		ImportNode node = parser.parseImport()
		
		then:
		thrown(IllegalImportChipmunk)
	}
	
	def "parse from foo.bar import baz"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("from foo.bar import baz")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		ImportNode node = parser.parseImport()
		
		then:
		node.toString() == "(import baz from foo.bar)"
	}
	
	def "parse from foo.bar import baz as biz"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("from foo.bar import baz as biz")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		ImportNode node = parser.parseImport()
		
		then:
		node.toString() == "(import baz as biz from foo.bar)"
	}
	
	def "parse from foo.bar import baz, baz2"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("from foo.bar import baz, baz2")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		ImportNode node = parser.parseImport()
		
		then:
		node.toString() == "(import baz,baz2 from foo.bar)"
	}
	
	def "parse from foo.bar import baz, baz2 as baz3, baz4"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("from foo.bar import baz, baz2 as baz3, baz4")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		ImportNode node = parser.parseImport()
		
		then:
		node.toString() == "(import baz,baz2 as baz3,baz4 from foo.bar)"
	}
	
	def "parse from foo.bar import *"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("from foo.bar import *")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		ImportNode node = parser.parseImport()
		
		then:
		node.toString() == "(import * from foo.bar)"
	}
	
	def "parse from foo.bar import * as baz"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("from foo.bar import * as baz")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		ImportNode node = parser.parseImport()
		
		then:
		thrown(IllegalImportChipmunk)
	}
	
	def "parse from foo.bar import bar,*"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("from foo.bar import bar, *")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		ImportNode node = parser.parseImport()
		
		then:
		thrown(SyntaxErrorChipmunk)
	}
	
	def "parse from foo.bar import bar,baz as bar1, baz1, baz2"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("from foo.bar import bar,baz as bar1, baz1, baz2")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		ImportNode node = parser.parseImport()
		
		then:
		thrown(IllegalImportChipmunk)
	}
	
	def "parse import foo.bar as bar1,baz"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("import foo.bar as bar1,baz")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		ImportNode node = parser.parseImport()
		
		then:
		thrown(IllegalImportChipmunk)
	}
}
