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

import java.io.InputStream;
import java.util.*;

import chipmunk.compiler.ast.*;
import chipmunk.compiler.imports.NativeImportResolver;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.modules.lang.LangModule;
import chipmunk.vm.ModuleLoader;
import chipmunk.binary.BinaryModule;
import chipmunk.compiler.ast.transforms.*;
import chipmunk.compiler.codegen.ModuleVisitor;
import chipmunk.compiler.imports.AstImportResolver;
import chipmunk.compiler.imports.BinaryImportResolver;
import chipmunk.compiler.lexer.ChipmunkLexer;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenStream;
import chipmunk.compiler.parser.ChipmunkParser;

public class ChipmunkCompiler {

	public enum Pass {
		POST_PARSE, SYMBOL_RESOLUTION, IMPORT_RESOLUTION, PRE_ASSEMBLY
	}

	protected Map<Pass, List<AstVisitor>> passes;
	protected ModuleLoader moduleLoader;

	protected final AstImportResolver astResolver;
	protected final BinaryImportResolver binaryResolver;
	protected final NativeImportResolver nativeResolver;
	
	public ChipmunkCompiler(){
		this(new ModuleLoader());
	}

	public ChipmunkCompiler(ModuleLoader loader){
		astResolver = new AstImportResolver();
		binaryResolver = new BinaryImportResolver(loader);
		nativeResolver = new NativeImportResolver(loader);
		loader.registerNativeFactory(LangModule.MODULE_NAME, LangModule::new);

		passes = new HashMap<>();
		passes.put(Pass.POST_PARSE, Arrays.asList(
				new LangImportVisitor(),
				new InitializerBuilderVisitor()));

		passes.put(Pass.SYMBOL_RESOLUTION, Arrays.asList(
				new SymbolTableBuilderVisitor(),
				new ConstructorVisitor()));

		passes.put(Pass.IMPORT_RESOLUTION, Arrays.asList(
				new ImportResolverVisitor(Arrays.asList(astResolver, binaryResolver, nativeResolver)))
		);

		passes.put(Pass.PRE_ASSEMBLY, Arrays.asList(
				new InnerMethodRewriteVisitor(),
				new SymbolAccessRewriteVisitor()));
	}

	public ModuleLoader getModuleLoader(){
		return binaryResolver.getModuleLoader();
	}

	public void setModuleLoader(ModuleLoader loader){
		binaryResolver.setModuleLoader(loader);
		nativeResolver.setModuleLoader(loader);
		loader.registerNativeFactory(LangModule.MODULE_NAME, LangModule::new);
	}

	public AstImportResolver getAstResolver(){
		return astResolver;
	}

	public BinaryImportResolver getBinaryResolver(){
		return binaryResolver;
	}

	public NativeImportResolver getNativeResolver() {
		return nativeResolver;
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

	public void visitAst(ModuleNode node, List<AstVisitor> visitors){
		visitors.forEach(v -> v.visit(node));
	}

	public BinaryModule[] compile(InputStream src, String fileName) throws CompileChipmunk {
		Compilation compilation = new Compilation();
		compilation.addSource(new ChipmunkSource(src, fileName));
		return compile(compilation);
	}

	public BinaryModule[] compile(Compilation compilation) throws CompileChipmunk {
		List<ModuleNode> asts = new ArrayList<>();

		for(ChipmunkSource source : compilation.getSources()){
			List<ModuleNode> parsed = parse(lex(source.readFully()), source.getFileName());
			asts.addAll(parsed);
		}

		return compile(asts);
	}

	public BinaryModule[] compile(ModuleNode... asts) throws CompileChipmunk {
		return compile(Arrays.asList(asts));
	}

	public BinaryModule[] compile(List<ModuleNode> asts) throws CompileChipmunk {
		astResolver.setModules(asts);

		asts.forEach(moduleNode -> visitAst(moduleNode, passes.get(Pass.POST_PARSE)));
		asts.forEach(moduleNode -> visitAst(moduleNode, passes.get(Pass.SYMBOL_RESOLUTION)));
		asts.forEach(moduleNode -> visitAst(moduleNode, passes.get(Pass.IMPORT_RESOLUTION)));
		asts.forEach(moduleNode -> visitAst(moduleNode, passes.get(Pass.PRE_ASSEMBLY)));

		BinaryModule[] modules = new BinaryModule[asts.size()];
		for(int i = 0; i < asts.size(); i++){
			ModuleNode ast = asts.get(i);

			ModuleVisitor visitor = new ModuleVisitor();
			ast.visit(visitor);

			BinaryModule module = visitor.getModule();
			module.setFileName(ast.getFileName());
			modules[i] = module;
		}

		return modules;
	}

	public BinaryModule compileExpression(String exp) throws CompileChipmunk {
		ModuleNode module = new ModuleNode();
		module.setName("exp");
		module.setFileName("runtimeExpression");

		MethodNode method = new MethodNode("evaluate");
		AstNode ret = new AstNode(NodeType.FLOW_CONTROL, new Token("return", TokenType.RETURN));

		TokenStream tokens = lex(exp);
		ChipmunkParser parser = new ChipmunkParser(tokens);

		ret.getChildren().add(parser.parseExpression());

		method.addToBody(ret);

		module.addMethodDef(method);

		return compile(module)[0];
	}

	public BinaryModule compileMethod(String methodDef) throws CompileChipmunk {
		ModuleNode module = new ModuleNode();
		module.setName("exp");
		module.setFileName("runtimeMethod");

		TokenStream tokens = lex(methodDef);
		ChipmunkParser parser = new ChipmunkParser(tokens);

		MethodNode method = parser.parseMethodDef();

		module.addMethodDef(method);

		return compile(module)[0];
	}

	public static String importedModuleName(String moduleName){
		return "$" + moduleName.replace('.', '_');
	}

}
