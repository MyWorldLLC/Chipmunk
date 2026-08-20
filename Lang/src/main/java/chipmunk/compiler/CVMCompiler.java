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
import chipmunk.compiler.ast.transforms.*;
import chipmunk.compiler.imports.AstImportResolver;
import chipmunk.compiler.imports.BinaryImportResolver;
import chipmunk.compiler.imports.NativeImportResolver;
import chipmunk.compiler.lexer.ChipmunkLexer;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenStream;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.parser.ChipmunkParser;
import chipmunk.compiler.symbols.Symbol;
import chipmunk.compiler.types.BuiltinTypes;
import chipmunk.compiler.types.ObjectType;
import chipmunk.modules.lang.LangModule;
import chipmunk.runtime.ChipmunkModule;
import chipmunk.vm.ModuleLoader;
import chipmunk.vm.invoke.Binder;

import java.io.InputStream;
import java.lang.classfile.*;
import java.lang.constant.*;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AccessFlag;
import java.util.*;
import java.util.function.Consumer;
import static java.lang.constant.ConstantDescs.*;

public class CVMCompiler {

    public enum Pass {
        POST_PARSE, SYMBOL_RESOLUTION, IMPORT_RESOLUTION, PRE_ASSEMBLY
    }

    private static final Map<ObjectType, Consumer<CodeBuilder>> returnGenerators;
    static {
        returnGenerators = new IdentityHashMap<>();
        returnGenerators.put(BuiltinTypes.ANY, CodeBuilder::areturn);
        returnGenerators.put(BuiltinTypes.BOOLEAN, CodeBuilder::ireturn);
        returnGenerators.put(BuiltinTypes.BYTE, CodeBuilder::ireturn);
        returnGenerators.put(BuiltinTypes.SHORT, CodeBuilder::ireturn);
        returnGenerators.put(BuiltinTypes.INT, CodeBuilder::ireturn);
        returnGenerators.put(BuiltinTypes.LONG, CodeBuilder::lreturn);
        returnGenerators.put(BuiltinTypes.FLOAT, CodeBuilder::freturn);
        returnGenerators.put(BuiltinTypes.DOUBLE, CodeBuilder::dreturn);
        returnGenerators.put(BuiltinTypes.STRING, CodeBuilder::areturn);
        returnGenerators.put(BuiltinTypes.MAP, CodeBuilder::areturn);
        returnGenerators.put(BuiltinTypes.LIST, CodeBuilder::areturn);
    }

    protected Map<Pass, List<AstVisitor>> passes;
    protected ModuleLoader moduleLoader;
    protected final CompilerConfig config;

    protected final AstImportResolver astResolver;
    protected final BinaryImportResolver binaryResolver;
    protected final NativeImportResolver nativeResolver;

    private final Map<ObjectType, ClassDesc> typeMapping;

    public CVMCompiler(){
        this(CompilerConfig.DEFAULT, new ModuleLoader());
    }

    public CVMCompiler(CompilerConfig config, ModuleLoader loader){
        astResolver = new AstImportResolver();
        binaryResolver = new BinaryImportResolver(loader);
        nativeResolver = new NativeImportResolver(loader);

        typeMapping = new IdentityHashMap<>();
        initBuiltinTypes();

        this.config = config;
        loader.registerNativeFactory(LangModule.MODULE_NAME, LangModule::new);

        passes = new HashMap<>();
        passes.put(Pass.POST_PARSE, Arrays.asList(
                new LangImportVisitor(),
                new LambdaReturnVisitor(),
                new InitializerBuilderVisitor()));

        passes.put(Pass.SYMBOL_RESOLUTION, Arrays.asList(
                new SymbolTableBuilderVisitor(),
                new ConstructorVisitor()));

        passes.put(Pass.IMPORT_RESOLUTION, Arrays.asList(
                new ImportResolverVisitor(Arrays.asList(astResolver, binaryResolver, nativeResolver)))
        );

        passes.put(Pass.PRE_ASSEMBLY, Arrays.asList(
                new SymbolAccessRewriteVisitor(),
                new InnerMethodRewriteVisitor(),
                new TypeInferenceVisitor())); // TODO - type checking
    }

    protected ClassDesc descriptorFor(Class<?> cls){
        return ClassDesc.of(cls.getName());
    }

    protected ClassDesc descriptorFor(ObjectType type){
        if(type == null){
            return descriptorFor(BuiltinTypes.ANY);
        }
        var desc = typeMapping.get(type);
        if(desc == null){
            desc = ClassDesc.of(type.name()); // TODO - qualified names
            typeMapping.put(type, desc);
        }
        return desc;
    }

    private void initBuiltinTypes(){
        typeMapping.put(BuiltinTypes.ANY, ConstantDescs.CD_Object);
        typeMapping.put(BuiltinTypes.BOOLEAN, ConstantDescs.CD_boolean);
        typeMapping.put(BuiltinTypes.BYTE, ConstantDescs.CD_byte);
        typeMapping.put(BuiltinTypes.SHORT, ConstantDescs.CD_short);
        typeMapping.put(BuiltinTypes.INT, ConstantDescs.CD_int);
        typeMapping.put(BuiltinTypes.LONG, ConstantDescs.CD_long);
        typeMapping.put(BuiltinTypes.FLOAT, ConstantDescs.CD_float);
        typeMapping.put(BuiltinTypes.DOUBLE, ConstantDescs.CD_double);
        typeMapping.put(BuiltinTypes.STRING, ConstantDescs.CD_String);
        typeMapping.put(BuiltinTypes.LIST, descriptorFor(Map.class));
        typeMapping.put(BuiltinTypes.MAP, descriptorFor(List.class));
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

    public List<AstNode> parse(TokenStream tokens, String sourceName) throws CompileChipmunk {
        ChipmunkParser parser = new ChipmunkParser(tokens);
        parser.setFileName(sourceName);
        parser.parse();
        return parser.getModuleRoots();
    }

    public void visitAst(AstNode node, List<AstVisitor> visitors){
        visitors.forEach(v -> {
            try{
                v.visit(node);
            } catch (Exception e) {
                System.out.println(node);
                throw e;
            }
        });
    }

    public Map<String, byte[]> compile(InputStream src, String fileName) throws CompileChipmunk {
        Compilation compilation = new Compilation();
        compilation.addSource(new ChipmunkSource(src, fileName));
        return compile(compilation);
    }

    public Map<String, byte[]> compile(Compilation compilation) throws CompileChipmunk {
        var asts = new ArrayList<ParsedModule>();

        for(ChipmunkSource source : compilation.getSources()){
            List<AstNode> parsed = parse(lex(source.readFully()), source.getFileName());
            parsed.forEach(n -> asts.add(new ParsedModule(source.getFileName(), n)));
        }

        return compile(asts);
    }

    public Map<String, byte[]> compile(AstNode... asts) throws CompileChipmunk {
        return compile(Arrays.stream(asts).map(a -> new ParsedModule("<memory>", a)).toList());
    }

    public Map<String, byte[]> compile(ParsedModule... modules) throws CompileChipmunk {
        return compile(Arrays.asList(modules));
    }

    public Map<String, byte[]> compile(List<ParsedModule> parsedModules) throws CompileChipmunk {
        astResolver.setModules(parsedModules.stream().map(ParsedModule::ast).toList());

        parsedModules.forEach(p -> visitAst(p.ast(), passes.get(Pass.POST_PARSE)));
        parsedModules.forEach(p -> visitAst(p.ast(), passes.get(Pass.SYMBOL_RESOLUTION)));
        parsedModules.forEach(p -> visitAst(p.ast(), passes.get(Pass.IMPORT_RESOLUTION)));
        parsedModules.forEach(p -> visitAst(p.ast(), passes.get(Pass.PRE_ASSEMBLY)));

        Map<String, byte[]> modules = new HashMap<>();
        for(int i = 0; i < parsedModules.size(); i++){
            var parsed = parsedModules.get(i);
            var ast = parsed.ast();
            modules.put(Modules.getName(ast).getName(), generateCode(parsed));
        }

        return modules;
    }

    public byte[] compileExpression(String exp) throws CompileChipmunk {
        AstNode module = Modules.make("exp");

        AstNode method = Methods.make("evaluate");
        AstNode ret = new AstNode(NodeType.FLOW_CONTROL, new Token("return", TokenType.RETURN));

        TokenStream tokens = lex(exp);
        ChipmunkParser parser = new ChipmunkParser(tokens);

        ret.addChild(parser.parseExpression());

        Methods.addToBody(method, ret);

        module.addChild(method);

        return compile(new ParsedModule("runtimeExpression", module)).get("exp");
    }

    public byte[] compileMethod(String methodDef) throws CompileChipmunk {
        AstNode module = Modules.make("exp");

        TokenStream tokens = lex(methodDef);
        ChipmunkParser parser = new ChipmunkParser(tokens);

        AstNode method = parser.parseMethodDef();

        module.addChild(method);

        return compile(module).get("exp");
    }

    protected byte[] generateCode(ParsedModule module) throws CompileChipmunk {
        return genClass(Modules.getName(module.ast()),
                cls -> {
                    cls.withInterfaceSymbols(ClassDesc.of(ChipmunkModule.class.getName()));
                    var ast = module.ast();
                    for(var element : ast.getChildren()){
                        switch (element.getNodeType()){
                            case VAR_DEC -> genVarDec(cls, ast, element);
                            case METHOD -> genMethod(cls, element);
                            case CLASS -> {
                                // TODO
                            }
                        }
                    }
        });
    }

    private byte[] genClass(Symbol symbol, Consumer<ClassBuilder> builder){
        return ClassFile.of().build(ClassDesc.of(symbol.getName()),
                cls -> {
                    cls.withFlags(AccessFlag.PUBLIC)
                       .withMethodBody(ConstantDescs.INIT_NAME, ConstantDescs.MTD_void,
                            ClassFile.ACC_PUBLIC,
                            cob -> cob.aload(0)
                                    // TODO - proper handling of Chipmunk class init
                                    .invokespecial(ConstantDescs.CD_Object,
                                            ConstantDescs.INIT_NAME, ConstantDescs.MTD_void)
                                    .return_());

                    builder.accept(cls);
                });
    }

    private void genMethod(ClassBuilder cls, AstNode method){
        var rType = descriptorFor(method.getResultType());

        var pList = new ArrayList<ClassDesc>();

        Methods.visitParams(method, (param) -> pList.add(descriptorFor(param.getResultType())));
        pList.removeFirst(); // Drop first one since that's always 'self'

        cls.withMethodBody(Methods.getName(method).getName(), MethodTypeDesc.of(rType, pList), ClassFile.ACC_PUBLIC, code -> {

            Methods.visitBody(method, node -> genStatement(code, node));

            // Fallback return
            code.aconst_null();
            code.areturn();
        });
    }

    private void genStatement(CodeBuilder code, AstNode statement){
        switch (statement.getNodeType()){
            case FLOW_CONTROL -> genFlowControl(code, statement);
            case WHILE -> genWhileLoop(code, statement);
        }
    }

    private void genVarDec(ClassBuilder cls, AstNode declaring, AstNode varDec){
        var name = VarDec.getVarName(varDec);
        cls.withField(name, ClassDesc.of(Object.class.getName()),
                field -> {
                    var symbol = declaring.getSymbolTable().getSymbol(name);
                    var flags = name.startsWith("$") ? ClassFile.ACC_PRIVATE : ClassFile.ACC_PUBLIC;
                    if(symbol.isFinal()){
                        flags += ClassFile.ACC_FINAL;
                    }
                    if(symbol.isShared()){
                        flags += ClassFile.ACC_STATIC;
                    }
                    field.withFlags(flags);
                });
    }

    private void genExpression(CodeBuilder code, AstNode exp){
        markLineNumber(code, exp);
        switch(exp.getNodeType()){
            case LITERAL -> {
                // TODO - double/byte/short/long literals
                switch (exp.getToken().type()) {
                    case BOOLLITERAL -> code.loadConstant(Boolean.parseBoolean(exp.getToken().text()) ? 1 : 0);
                    case INTLITERAL ->
                        code.loadConstant(Integer.parseInt(exp.getToken().text().replace("_", ""), 10));
                    case HEXLITERAL ->
                        code.loadConstant(Integer.parseInt(exp.getToken().text().replace("_", "").substring(2), 16));
                    case OCTLITERAL ->
                        code.loadConstant(Integer.parseInt(exp.getToken().text().replace("_", "").substring(2), 8));
                    case BINARYLITERAL ->
                        code.loadConstant(Integer.parseInt(exp.getToken().text().replace("_", "").substring(2), 2));
                    case FLOATLITERAL ->
                        code.loadConstant(Float.parseFloat(exp.getToken().text()));
                    case STRINGLITERAL -> {
                        // strip quotes
                        String value = exp.getToken().text().substring(1, exp.getToken().text().length() - 1);
                        code.loadConstant(ChipmunkLexer.unescapeString(value));
                    }
                    case NULL -> code.aconst_null();
                }
            }
            case LIST -> {
                genNewInstance(code, ArrayList.class);
                var CD_list = ClassDesc.of(ArrayList.class.getName());
                exp.getChildren().forEach(child -> {
                    code.dup();
                    genExpression(code, child);
                    if(!child.getResultType().isAssignableTo(BuiltinTypes.ANY)){
                        // Promote primitives
                        genConversion(code, BuiltinTypes.ANY, child.getResultType());
                    }
                    code.invokevirtual(CD_list, "add", MethodTypeDesc.of(CD_boolean, CD_Object));
                    code.pop(); // Pop the boolean result of add()
                });
            }
            case MAP -> {
                genNewInstance(code, HashMap.class);
                var CD_map = ClassDesc.of(HashMap.class.getName());
                exp.getChildren().forEach(child -> {
                    code.dup();
                    var key = child.getLeft();
                    var value = child.getRight();

                    genExpression(code, key);
                    // Promote primitives
                    genConversion(code, BuiltinTypes.ANY, key.getResultType());

                    genExpression(code, value);
                    // Promote primitives
                    genConversion(code, BuiltinTypes.ANY, value.getResultType());

                    code.invokevirtual(CD_map, "put", MethodTypeDesc.of(CD_Object, CD_Object, CD_Object));
                    code.pop(); // Pop the Object result of put()
                });
            }
            case OPERATOR -> {
                Token operator = exp.getToken();

                var tmpOperands = exp.getChildren();
                var operation = operator.text();

                var lhs = exp.getLeft();
                switch (operator.type()){
                    // Assignment
                    case EQUALS -> {
                        if(lhs.is(NodeType.OPERATOR)){
                            if(lhs.getToken().type() == TokenType.DOT){
                                /*assembler.onLine(lhs.getLineNumber());
                                lhs.getLeft().visit(this);
                                String attr = lhs.getRight().getToken().text();

                                assembler.onLine(op.getRight().getLineNumber());
                                op.getRight().visit(this);

                                assembler.onLine(lhs.getLineNumber());
                                assembler.setattr(attr);*/
                            }else if(lhs.getToken().type() == TokenType.LBRACKET){
                                tmpOperands = List.of(lhs.getLeft(), lhs.getRight(), exp.getRight());
                                operation = Intrinsics.SET_AT;
                            }else{
                                // error!
                                throw new CompileChipmunk(String.format("Invalid assignment at %d. The left hand side of an assignment"
                                                + "must be either an attribute, index, or a local variable.",
                                        lhs.getToken().line()));
                            }
                        }else if(lhs.is(NodeType.ID)){
                            // Local assignment
                            /*assembler.onLine(lhs.getLineNumber());
                            op.getRight().visit(this);
                            codegen.emitLocalAssignment(lhs.getToken().text());*/
                        }
                    }
                }

                var operands = tmpOperands;
                var operandTypes = tmpOperands.stream()
                        .map(AstNode::getResultType)
                        .map(t -> t == null ? BuiltinTypes.ANY : t) // Replace unspecified type with Any
                        .toArray(ObjectType[]::new);

                var emitOp = operation;
                Intrinsics.getEmitter(operation, operandTypes)
                        .ifPresentOrElse(emitter -> {

                            for(int i = 0; i < operands.size(); i++){
                                var operand = operands.get(i);
                                genExpression(code, operand);
                                // Since this is an intrinsic we know the types are convertible
                                genConversion(code, emitter.op().pValues()[i], operandTypes[i]);
                            }

                            emitter.emitter().accept(code);
                        }, () -> {

                            for(int i = 0; i < operands.size(); i++){
                                var operand = operands.get(i);
                                genExpression(code, operand);
                                // This is a dynamic call, so box any primitives
                                genConversion(code, BuiltinTypes.ANY, operandTypes[i]);
                            }

                            genDynamicInvocation(code, emitOp, operands.size());
                        });
            }
        }
    }

    private void genDynamicInvocation(CodeBuilder code, String op, int argc){
        var objType = ClassDesc.of(Object.class.getName());
        ClassDesc[] pTypes = new ClassDesc[argc];
        Arrays.fill(pTypes, objType);

        var callType = MethodTypeDesc.of(objType, pTypes);

        var CD_Binder = ClassDesc.of(Binder.class.getName());
        var CD_CallSite = ClassDesc.of(CallSite.class.getName());
        var CD_MHLookup = ClassDesc.of(MethodHandles.Lookup.class.getName());
        var CD_MType = ClassDesc.of(MethodType.class.getName());
        var bootstrapDescriptor = MethodTypeDesc.of(CD_CallSite, CD_MHLookup, CD_String, CD_MType).descriptorString();

        code.invokedynamic(DynamicCallSiteDesc.of(
                MethodHandleDesc.of(DirectMethodHandleDesc.Kind.STATIC, CD_Binder,
                        Binder.INDY_BOOTSTRAP_METHOD, bootstrapDescriptor), op, callType));
    }

    private void genNewInstance(CodeBuilder code, Class<?> type){
        var typeDesc = ClassDesc.of(type.getName());
        code.new_(typeDesc)
                .dup()
                .invokespecial(typeDesc, "<init>", MethodTypeDesc.of(CD_void));
    }

    private void genConversion(CodeBuilder code, ObjectType to, ObjectType from){
        if(!from.isAssignableTo(to)){
            var conversion = Intrinsics.getConversion(to, from)
                    .orElseThrow(() -> new IllegalArgumentException("Cannot convert from " + from.name() + " to " + to.name() + ". This is a compiler bug."));

            conversion.emitter().accept(code);
        }
    }

    private void genFlowControl(CodeBuilder code, AstNode f){
        switch (f.getToken().type()) {
            case RETURN -> genReturn(code, f);
            // TODO - throw/break/continue
        }
    }

    private void genWhileLoop(CodeBuilder code, AstNode loop){
        // TODO - suspension support
        code.block(block -> {
            var guard = loop.getChild(0);
            genExpression(code, guard);
            // TODO - handle boxing/conversion to boolean if needed
            code.ifeq(block.breakLabel());
            loop.visitChildren(node -> genStatement(code, node), 1);
            code.goto_(block.startLabel());
        });
    }

    private void genForLoop(CodeBuilder code, AstNode loop){
        // TODO - suspension support
        // Get the iterator
        var symbols = loop.getSymbolTable();
        var iter = loop.getChild();
        var iterName = iter.getSymbol();
        var iterLocal = symbols.getLocalIndex(iterName);

        var id = iter.getLeft();
        var idName = id.getSymbol();
        var idLocal = symbols.getLocalIndex(idName);

        // Create and store the iterator before entering the loop
        genExpression(code, iter.getRight());
        code.storeLocal(TypeKind.REFERENCE, iterLocal);

        code.block(block -> {
            // TODO - handle dynamic invocation & truthiness conversion if needed
            // 'hasNext' guards the loop
            code.loadLocal(TypeKind.REFERENCE, iterLocal);
            code.invokevirtual(descriptorFor(Iterator.class), "hasNext", MethodTypeDesc.of(CD_boolean));
            code.ifne(block.breakLabel());

            // First task is calling the iterator and storing the result
            code.loadLocal(TypeKind.REFERENCE, iterLocal);
            code.invokevirtual(descriptorFor(Iterator.class), "next", MethodTypeDesc.of(CD_Object)); // TODO - actual type
            code.storeLocal(TypeKind.REFERENCE, idLocal);

            loop.visitChildren(node -> genStatement(code, node), 1);

            code.goto_(block.startLabel());
        });
    }

    private void genReturn(CodeBuilder code, AstNode rNode){

        if(rNode.hasChildren()){
            genExpression(code, rNode.getChild());
        }else{
            code.aconst_null();
        }

        var type = rNode.getResultType();
        if(type == null){
            type = BuiltinTypes.ANY;
        }

        var expectedType = getExpectedReturnType(rNode);
        if(!type.isAssignableTo(expectedType)){
            promoteType(code, expectedType, type);
        }

        markLineNumber(code, rNode);
        var generator = returnGenerators.get(type);
        generator.accept(code);
    }

    private void promoteType(CodeBuilder code, ObjectType expected, ObjectType actual){
        var converter = Intrinsics.getConversion(expected, actual)
                .orElseThrow(() -> new IllegalArgumentException("Cannot convert from " + expected.name() + " to " + actual.name() + ". This is a compiler bug."));
        converter.emitter().accept(code);
        /*if(BuiltinTypes.ANY == expected && BuiltinTypes.INTEGER == actual){
            code.invokestatic(ConstantDescs.CD_Integer, "valueOf", MethodTypeDesc.of(CD_Integer, CD_int));
        }else if(BuiltinTypes.ANY == expected && BuiltinTypes.BOOLEAN == actual){
            code.invokestatic(CD_Boolean, "valueOf", MethodTypeDesc.of(CD_Boolean, CD_boolean));
        }*/
    }

    protected ObjectType getExpectedReturnType(AstNode rNode){
        var parent = rNode.getParent();
        while(!parent.is(NodeType.METHOD)){
            parent = parent.getParent();
        }
        var rType = parent.getResultType();
        return rType != null ? rType : BuiltinTypes.ANY;
    }

    private void markLineNumber(CodeBuilder code, AstNode n){
        if(n.getLineNumber() != -1){
            code.lineNumber(n.getLineNumber());
        }
    }

    public static String importedModuleName(String moduleName){
        return "$" + moduleName.replace('.', '_');
    }

}
