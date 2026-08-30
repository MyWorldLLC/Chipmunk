/*
 * Copyright (C) 2026 MyWorld, LLC
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

import chipmunk.compiler.ast.*;
import chipmunk.compiler.imports.AstImportResolver;
import chipmunk.compiler.imports.BinaryImportResolver;
import chipmunk.compiler.imports.NativeImportResolver;
import chipmunk.compiler.ir.IRBuilder;
import chipmunk.compiler.ir.passes.EvaluationContext;
import chipmunk.compiler.ir.passes.EvaluationEnvironment;
import chipmunk.compiler.ir.passes.TypeResolutionContext;
import chipmunk.compiler.lexer.ChipmunkLexer;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenStream;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.parser.ChipmunkParser;
import chipmunk.modules.lang.LangModule;
import chipmunk.vm.ModuleLoader;
import chipmunk.vm.jvm.ChipmunkClassLoader;

import java.io.InputStream;
import java.util.*;

public class CVMCompiler {

    protected ModuleLoader moduleLoader;
    protected final CompilerConfig config;

    protected final AstImportResolver astResolver;
    protected final BinaryImportResolver binaryResolver;
    protected final NativeImportResolver nativeResolver;


    public CVMCompiler(){
        this(CompilerConfig.DEFAULT, new ModuleLoader());
    }

    public CVMCompiler(CompilerConfig config, ModuleLoader loader){
        astResolver = new AstImportResolver();
        binaryResolver = new BinaryImportResolver(loader);
        nativeResolver = new NativeImportResolver(loader);

        this.config = config;
        loader.registerNativeFactory(LangModule.MODULE_NAME, LangModule::new);

    }

    public ModuleLoader getModuleLoader(){
        return moduleLoader;
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

    public List<AstNode> parse(TokenStream tokens, String sourceName) throws CompileChipmunk {
        ChipmunkParser parser = new ChipmunkParser(tokens);
        parser.setFileName(sourceName);
        parser.parse();
        return parser.getModuleRoots();
    }

    public List<ModuleClasses> compile(InputStream src, String fileName) throws CompileChipmunk {
        Compilation compilation = new Compilation();
        compilation.addSource(new ChipmunkSource(src, fileName));
        return compile(compilation);
    }

    public List<ParsedModule> parseModules(Compilation compilation){
        var asts = new ArrayList<ParsedModule>();

        for(ChipmunkSource source : compilation.getSources()){
            List<AstNode> parsed = parse(lex(source.readFully()), source.getFileName());
            parsed.forEach(n -> asts.add(new ParsedModule(source.getFileName(), n)));
        }
        return asts;
    }

    public List<ModuleClasses> compile(Compilation compilation) throws CompileChipmunk {
        return compile(compilation, parseModules(compilation));
    }

    public List<ModuleClasses> compile(Compilation compilation, AstNode... asts) throws CompileChipmunk {
        return compile(compilation, Arrays.stream(asts).map(a -> new ParsedModule("<memory>", a)).toList());
    }

    public List<ModuleClasses> compile(Compilation compilation, ParsedModule... modules) throws CompileChipmunk {
        return compile(compilation, Arrays.asList(modules));
    }

    public List<ModuleClasses> compile(Compilation compilation, List<ParsedModule> parsedModules) throws CompileChipmunk {
        prepareAsts(parsedModules);

        var evalEnv = new EvaluationEnvironment(compilation);
        var irBuilder = new IRBuilder();

        var moduleIr = parsedModules.stream().map(m -> irBuilder.buildModule(evalEnv, m.ast())).toList();
        moduleIr.forEach(ir -> ir.markSymbols(evalEnv));
        moduleIr.forEach(ir -> {
            var ctx = new TypeResolutionContext();
            ir.resolveTypes(evalEnv, ctx);
            ctx.flushTasks();
        });
        moduleIr.forEach(ir -> ir.checkSemantics(evalEnv));

        var modules = new ArrayList<ModuleClasses>();
        for(int i = 0; i < parsedModules.size(); i++){
            var parsed = parsedModules.get(i);
            var ast = parsed.ast();
            var name = Modules.getName(ast).getName();
            var ir = moduleIr.get(i);
            var ctx = new EvaluationContext(compilation, evalEnv);
            ctx.evaluateModule(ir);
            modules.add(new ModuleClasses(name, name, ir, ctx.getEmittedClasses()));
        }

        return modules;
    }

    public void prepareAsts(List<ParsedModule> parsedModules) throws CompileChipmunk {
        astResolver.setModules(parsedModules.stream().map(ParsedModule::ast).toList());
    }

    public AstNode expressionEvalModule(String exp){
        var module = Modules.make("exp");

        var method = Methods.make("evaluate");
        var ret = new AstNode(NodeType.FLOW_CONTROL, new Token("return", TokenType.RETURN));

        var tokens = lex(exp);
        var parser = new ChipmunkParser(tokens);

        ret.addChild(parser.parseExpression());

        Methods.addToBody(method, ret);

        module.addChild(method);
        return module;
    }

    public byte[] compileExpression(String exp) throws CompileChipmunk {
        return compile(new Compilation(), new ParsedModule("runtimeExpression", expressionEvalModule(exp))).getFirst().classes().get("exp");
    }

    public AstNode methodEvalModule(String methodDef){
        var module = Modules.make("exp");

        var tokens = lex(methodDef);
        var parser = new ChipmunkParser(tokens);

        var method = parser.parseMethodDef();

        module.addChild(method);

        return module;
    }

    public byte[] compileMethod(String methodDef) throws CompileChipmunk {
        return compile(new Compilation(), new ParsedModule("runtimeMethod", methodEvalModule(methodDef))).getFirst().classes().get("exp");
    }

    public Class<?> bindingFor(ChipmunkClassLoader loader, String bindingName, Class<?> targetType, String methodName){
        return new CVMCodegen(astResolver, binaryResolver, nativeResolver).bindingFor(loader, bindingName, targetType, methodName);
    }

}
