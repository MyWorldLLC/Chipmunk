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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import chipmunk.binary.BinaryMethod;
import chipmunk.binary.BinaryModule;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.FlowControlNode;
import chipmunk.compiler.ast.MethodNode;
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

	public BinaryModule compileAst(ModuleNode node) throws CompileChipmunk {

		for(AstVisitor visitor : visitors){
			node.visit(visitor);
		}

		ModuleVisitor visitor = new ModuleVisitor();
		node.visit(visitor);
		return visitor.getModule();
	}
	
	public BinaryModule[] compile(CharSequence src, String fileName) throws CompileChipmunk {

		parsedModules = parse(lex(src), fileName);

		BinaryModule[] modules = new BinaryModule[parsedModules.size()];
		for(int i = 0; i < parsedModules.size(); i++){
			modules[i] = compileAst(parsedModules.get(i));
		}
		
		return modules;
	}

	public TokenStream lex(CharSequence src) throws CompileChipmunk {
		ChipmunkLexer lexer = new ChipmunkLexer();
		return lexer.lex(src);
	}

	public List<ModuleNode> parse(TokenStream tokens, String sourceName) throws CompileChipmunk {
		ChipmunkParser parser = new ChipmunkParser(tokens);
		parser.setFileName(sourceName);
		parser.parse();
		return parser.getModuleRoots();
	}
	
	public BinaryModule[] compile(InputStream src, String fileName) throws CompileChipmunk {
		StringBuilder builder = new StringBuilder();

		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(src, StandardCharsets.UTF_8));
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

	public BinaryModule compileExpression(String exp) throws CompileChipmunk {
		ModuleNode module = new ModuleNode();
		module.setName("exp");

		MethodNode method = new MethodNode("evaluate");
		FlowControlNode ret = new FlowControlNode(new Token("return", Token.Type.RETURN));

		TokenStream tokens = lex(exp);
		ChipmunkParser parser = new ChipmunkParser(tokens);

		ret.getChildren().add(parser.parseExpression());

		method.addToBody(ret);

		module.addMethodDef(method);

		return compileAst(module);
	}

	public BinaryModule compileMethod(String methodDef) throws CompileChipmunk {
		ModuleNode module = new ModuleNode();
		module.setName("exp");

		TokenStream tokens = lex(methodDef);
		ChipmunkParser parser = new ChipmunkParser(tokens);

		MethodNode method = parser.parseMethodDef();

		module.addMethodDef(method);

		return compileAst(module);
	}

}
