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

package chipmunk.compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.ModuleNode;
import chipmunk.compiler.codegen.InnerMethodRewriteVisitor;
import chipmunk.compiler.codegen.ModuleVisitor;
import chipmunk.compiler.codegen.SymbolTableBuilderVisitor;
import chipmunk.modules.runtime.CModule;

public class ChipmunkCompiler {
	
	protected List<AstVisitor> visitors;
	protected List<ModuleNode> parsedModules;
	
	public ChipmunkCompiler(){
		visitors = new ArrayList<>();
		visitors.add(new InnerMethodRewriteVisitor());
		visitors.add(new SymbolTableBuilderVisitor());
	}
	
	public List<AstVisitor> getVisitors(){
		return visitors;
	}
	
	public List<ModuleNode> getLastParsedModules(){
		return parsedModules;
	}
	
	public List<CModule> compile(CharSequence src, String fileName) throws CompileChipmunk {
		
		List<CModule> modules = new ArrayList<CModule>();
		ChipmunkLexer lexer = new ChipmunkLexer();
		TokenStream tokens = lexer.lex(src);
		
		ChipmunkParser parser = new ChipmunkParser(tokens);
		parser.setFileName(fileName);
		parser.parse();
		parsedModules = parser.getModuleRoots();
		
		for(ModuleNode node : parsedModules){
			
			for(AstVisitor visitor : visitors){
				node.visit(visitor);
			}
			
			ModuleVisitor visitor = new ModuleVisitor();
			node.visit(visitor);
			modules.add(visitor.getModule());
		}
		
		return modules;
	}
	
	public List<CModule> compile(InputStream src, String fileName) throws CompileChipmunk {
		StringBuilder builder = new StringBuilder();

		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(src, Charset.forName("UTF-8")));
			int character = reader.read();
			while(character != -1){
				builder.append((char) character);
				character = reader.read();
			}
		}catch(IOException ex){
			throw new CompileChipmunk("Failed to load source", ex);
		}

		
		return compile(builder, fileName);
	}

}
