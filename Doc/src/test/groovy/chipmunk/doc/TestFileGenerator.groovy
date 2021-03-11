/*
 * Copyright (C) 2021 MyWorld, LLC
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

package chipmunk.doc

import chipmunk.compiler.ChipmunkSource
import chipmunk.compiler.lexer.ChipmunkLexer
import chipmunk.doc.io.DocFileWriterFactory

import java.nio.file.Path

def outputDir = Path.of(System.getProperty("user.dir"), args[0]).normalize()

def writerFactory = new DocFileWriterFactory(outputDir)

def generator = new DocGenerator()
generator.setTemplateResource("templates")
generator.setWriterFactory(writerFactory)

generator.loadTemplates()

def sources = [
        '/chipmunk/doc/TestAllComments.chp',
        '/chipmunk/doc/TestSomeComments.chp'
]

sources = sources.collect {new ChipmunkSource(getClass().getResourceAsStream(it), it)}
ChipmunkLexer lexer = new ChipmunkLexer()

for(def source : sources){
    def stream = lexer.lex(source.readFully())
    generator.buildDocTree(stream, Path.of(source.fileName).getFileName().toString())
}

generator.generate()