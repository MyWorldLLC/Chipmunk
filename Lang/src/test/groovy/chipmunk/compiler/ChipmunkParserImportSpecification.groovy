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

import chipmunk.compiler.ast.AstNode
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
		AstNode node = parser.parseImport()
		
		then:
		thrown(IllegalImportException)
	}
	
	def "parse import foo.bar"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("import foo.bar")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		AstNode node = parser.parseImport()
		
		then:
		node.toString() == "(import import (id foo) (import import (id bar)))"
	}
	
	def "parse import foo.bar.baz"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("import foo.bar.baz")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		AstNode node = parser.parseImport()
		
		then:
		node.toString() == "(import import (id foo.bar) (import import (id baz)))"
	}
	
	def "parse import foo.bar.*"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("import foo.bar.*")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		AstNode node = parser.parseImport()
		
		then:
		node.toString() == "(import import (id foo.bar) (import import (id *)))"
	}
	
	def "parse import foo.bar as baz"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("import foo.bar as baz")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		AstNode node = parser.parseImport()
		
		then:
		node.toString() == "(import import (id foo) (import import (id bar)) (import as (id baz)))"
	}
	
	def "parse import foo.bar.* as baz"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("import foo.bar.* as baz")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		AstNode node = parser.parseImport()
		
		then:
		thrown(IllegalImportException)
	}
	
	def "parse from foo.bar import baz"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("from foo.bar import baz")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		AstNode node = parser.parseImport()
		
		then:
		node.toString() == "(import from (id foo.bar) (import import (id baz)))"
	}
	
	def "parse from foo.bar import baz as biz"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("from foo.bar import baz as biz")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		AstNode node = parser.parseImport()
		
		then:
		node.toString() == "(import from (id foo.bar) (import import (id baz)) (import as (id biz)))"
	}
	
	def "parse from foo.bar import baz, baz2"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("from foo.bar import baz, baz2")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		AstNode node = parser.parseImport()
		
		then:
		node.toString() == "(import from (id foo.bar) (import import (id baz) (id baz2)))"
	}
	
	def "parse from foo.bar import baz, baz2 as baz3, baz4"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("from foo.bar import baz, baz2 as baz3, baz4")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		AstNode node = parser.parseImport()
		
		then:
		node.toString() == "(import from (id foo.bar) (import import (id baz) (id baz2)) (import as (id baz3) (id baz4)))"
	}
	
	def "parse from foo.bar import *"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("from foo.bar import *")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		AstNode node = parser.parseImport()
		
		then:
		node.toString() == "(import from (id foo.bar) (import import (id *)))"
	}
	
	def "parse from foo.bar import * as baz"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("from foo.bar import * as baz")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		AstNode node = parser.parseImport()
		
		then:
		thrown(IllegalImportException)
	}
	
	def "parse from foo.bar import bar,*"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("from foo.bar import bar, *")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		AstNode node = parser.parseImport()
		
		then:
		thrown(IllegalImportException)
	}
	
	def "parse from foo.bar import bar,baz as bar1, baz1, baz2"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("from foo.bar import bar,baz as bar1, baz1, baz2")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		AstNode node = parser.parseImport()
		
		then:
		thrown(IllegalImportException)
	}
	
	def "parse import foo.bar as bar1,baz"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("import foo.bar as bar1,baz")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		AstNode node = parser.parseImport()
		
		then:
		thrown(IllegalImportException)
	}
}
