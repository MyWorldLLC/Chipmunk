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
import chipmunk.compiler.parser.parselets.LiteralParselet;
import chipmunk.compiler.symbols.Symbol;
import chipmunk.compiler.symbols.SymbolTable;
import chipmunk.compiler.types.*;
import chipmunk.modules.lang.LangModule;
import chipmunk.runtime.CRuntime;
import chipmunk.runtime.ChipmunkClass;
import chipmunk.runtime.ChipmunkModule;
import chipmunk.runtime.MethodBinding;
import chipmunk.vm.ChipmunkVM;
import chipmunk.vm.ModuleLoader;
import chipmunk.vm.invoke.Binder;
import chipmunk.vm.invoke.security.AllowChipmunkLinkage;
import chipmunk.vm.jvm.ChipmunkClassLoader;

import java.io.InputStream;
import java.lang.classfile.*;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.lang.classfile.attribute.SourceFileAttribute;
import java.lang.constant.*;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AccessFlag;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.constant.ConstantDescs.*;

public class CVMCompiler {

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

    protected List<AstVisitor> passes;
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

        passes = List.of(
                new LangImportVisitor(),
                new LambdaReturnVisitor(),
                new InitializerBuilderVisitor(),
                new SymbolTableBuilderVisitor(),
                new ConstructorVisitor(),
                new ImportResolverVisitor(Arrays.asList(astResolver, binaryResolver, nativeResolver)),
                new SymbolAccessRewriteVisitor(),
                new TypeInferenceVisitor(),
                new VarInitRewriteVisitor(),
                new InnerMethodRewriteVisitor()
        ); // TODO - type checking
    }

    protected ClassDesc descriptorFor(Class<?> cls){
        if(cls.isPrimitive()){
            var mapping = new HashMap<Class<?>, ClassDesc>();
            mapping.put(boolean.class, CD_boolean);
            mapping.put(byte.class, CD_byte);
            mapping.put(short.class, CD_short);
            mapping.put(int.class, CD_int);
            mapping.put(long.class, CD_long);
            mapping.put(float.class, CD_float);
            mapping.put(double.class, CD_double);
            return mapping.get(cls);
        }
        return ClassDesc.of(cls.getName());
    }

    protected ClassDesc descriptorFor(ObjectType type){
        if(type == null){
            return descriptorFor(BuiltinTypes.ANY);
        }
        if(type instanceof chipmunk.compiler.types.MethodType){
            return descriptorFor(MethodBinding.class);
        }
        var desc = typeMapping.get(type);
        if(desc == null){
            desc = ClassDesc.of(type.name()); // TODO - qualified names
            typeMapping.put(type, desc);
        }
        return desc;
    }

    private void initBuiltinTypes(){
        typeMapping.put(BuiltinTypes.ANY, CD_Object);
        typeMapping.put(BuiltinTypes.BOOLEAN, CD_boolean);
        typeMapping.put(BuiltinTypes.BYTE, CD_byte);
        typeMapping.put(BuiltinTypes.SHORT, CD_short);
        typeMapping.put(BuiltinTypes.INT, CD_int);
        typeMapping.put(BuiltinTypes.LONG, CD_long);
        typeMapping.put(BuiltinTypes.FLOAT, CD_float);
        typeMapping.put(BuiltinTypes.DOUBLE, CD_double);
        typeMapping.put(BuiltinTypes.STRING, CD_String);
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
        visitors.forEach(v -> v.visit(node));
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
        return compile(parseModules(compilation));
    }

    public List<ModuleClasses> compile(AstNode... asts) throws CompileChipmunk {
        return compile(Arrays.stream(asts).map(a -> new ParsedModule("<memory>", a)).toList());
    }

    public List<ModuleClasses> compile(ParsedModule... modules) throws CompileChipmunk {
        return compile(Arrays.asList(modules));
    }

    public List<ModuleClasses> compile(List<ParsedModule> parsedModules) throws CompileChipmunk {
        prepareAsts(parsedModules);

        var modules = new ArrayList<ModuleClasses>();
        for(int i = 0; i < parsedModules.size(); i++){
            var parsed = parsedModules.get(i);
            var ast = parsed.ast();
            var name = Modules.getName(ast).getName();
            // TODO - package-prefixed module class name?
            modules.add(new ModuleClasses(name, name, generateCode(parsed)));
        }

        return modules;
    }

    public void prepareAsts(List<ParsedModule> parsedModules) throws CompileChipmunk {
        astResolver.setModules(parsedModules.stream().map(ParsedModule::ast).toList());
        parsedModules.forEach(p -> visitAst(p.ast(), passes));
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
        return compile(new ParsedModule("runtimeExpression", expressionEvalModule(exp))).getFirst().classes().get("exp");
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
        return compile(new ParsedModule("runtimeMethod", methodEvalModule(methodDef))).getFirst().classes().get("exp");
    }

    protected Map<String, byte[]> generateCode(ParsedModule module) throws CompileChipmunk {
        var state = new CompilerState();
        var moduleName = Modules.getName(module.ast()).getName();
        state.withClass(moduleName, bootstrapClass(Modules.getName(module.ast()),
                cls -> {
                    cls.withInterfaceSymbols(ClassDesc.of(ChipmunkModule.class.getName()));
                    cls.with(SourceFileAttribute.of(module.fileName()));
                    var ast = module.ast();

                    cls.withMethodBody("initialize", MethodTypeDesc.of(CD_void, ClassDesc.of(ChipmunkVM.class.getName())), ClassFile.ACC_PUBLIC, init ->
                            init.aload(0)
                                    .aload(1)
                                    .loadConstant("Hello from initializer of " + moduleName)
                                    .invokestatic(ClassDesc.of(CRuntime.class.getName()), "print", MethodTypeDesc.of(CD_void, CD_String))
                            .invokevirtual(ClassDesc.of(moduleName), "$module_init$", MethodTypeDesc.of(CD_Object, CD_Object))
                            .pop()
                            .return_());

                    // TODO - proper name nesting for classes

                    state.enterScope(ast.getSymbolTable());

                    for(var element : ast.getChildren()){
                        switch (element.getNodeType()){
                            case VAR_DEC -> genVarDec(cls, state, element);
                            case METHOD -> genMethod(cls, state, element);
                            case CLASS -> {
                                var clsName = element.getSymbol();
                                state.withClass(clsName.getName(), genClass(clsName, state, module.fileName(), element));
                            }
                        }
                    }
                    state.exitScope();
                }));
        return state.classes();
    }

    private byte[] genClass(Symbol symbol, CompilerState state, String fileName, AstNode ast) {
        return bootstrapClass(symbol, cls -> {
            cls.withInterfaceSymbols(ClassDesc.of(ChipmunkClass.class.getName()));
            cls.with(SourceFileAttribute.of(fileName));

            state.enterScope(ast.getSymbolTable());

            for(var element : ast.getChildren()){
                switch (element.getNodeType()){
                    case VAR_DEC -> genVarDec(cls, state, element);
                    case METHOD -> genMethod(cls, state, element);
                    case CLASS -> {
                        var clsName = element.getSymbol();
                        state.withClass(clsName.getName(), genClass(clsName, state, fileName, element));
                    }
                }
            }
            state.exitScope();
        });
    }

    private byte[] bootstrapClass(Symbol symbol, Consumer<ClassBuilder> builder){
        return ClassFile.of().build(ClassDesc.of(symbol.getName()),
                cls -> {
                    cls.withFlags(AccessFlag.PUBLIC)
                       .withMethodBody(INIT_NAME, MTD_void,
                            ClassFile.ACC_PUBLIC,
                            cob -> cob.aload(0)
                                    // TODO - proper handling of Chipmunk class init
                                    .invokespecial(CD_Object,
                                            INIT_NAME, MTD_void)
                                    .return_());

                    builder.accept(cls);
                });
    }

    private void genMethod(ClassBuilder cls, CompilerState state, AstNode method){
        state.enterScope(method.getSymbolTable());
        var rType = descriptorFor(method.getResultType());

        var pList = new ArrayList<ClassDesc>();

        Methods.visitParams(method, (param) -> pList.add(descriptorFor(param.getResultType())));
        pList.removeFirst(); // Drop first one since that's always 'self' & the this-ref is implicit in JVM bytecode

        cls.withMethodBody(Methods.getName(method).getName(), MethodTypeDesc.of(rType, pList), ClassFile.ACC_PUBLIC, code -> {

            Methods.visitBody(method, node -> genStatement(code, state, node));

            // Fallback return
            if(method.getResultType() instanceof PrimitiveType){
                switch (method.getResultType()){
                    case BooleanType _, IntegerType _ -> code.iconst_0().ireturn();
                    case FloatType _ -> code.fconst_0().freturn();
                    default -> code.aconst_null().areturn();
                }
            }else{
                code.aconst_null();
                code.areturn();
            }

        });
        state.exitScope();
    }

    private void genStatement(CodeBuilder code, CompilerState state, AstNode statement){
        switch (statement.getNodeType()){
            case VAR_DEC -> genLocalVarDec(code, state, statement);
            case FLOW_CONTROL -> genFlowControl(code, state, statement);
            case WHILE -> genWhileLoop(code, state, statement);
            case FOR -> genForLoop(code, state, statement);
            case IF_ELSE -> genIfElse(code, state, statement);
            default -> genExpression(code, state, statement);
        }
    }

    private void genVarDec(ClassBuilder cls, CompilerState state, AstNode varDec){
        var name = VarDec.getVarName(varDec);
        cls.withField(name, ClassDesc.of(Object.class.getName()),
                field -> {
                    var symbol = state.scope().getSymbol(name);
                    var flags = ClassFile.ACC_PUBLIC;
                    if(symbol.isFinal()){
                        flags += ClassFile.ACC_FINAL;
                    }
                    if(symbol.isShared()){
                        flags += ClassFile.ACC_STATIC;
                    }
                    field.withFlags(flags);
                });
    }

    private void genExpression(CodeBuilder code, CompilerState state, AstNode exp){
        markLineNumber(code, exp);
        switch(exp.getNodeType()){
            case ID -> {
                var symbol = state.scope().getSymbol(exp.getToken().text());
                emitLocalReference(code, state, symbol.getName(), symbol.getReferentType(), LocalAccess.LOAD, false);
            }
            case LITERAL -> {
                switch (exp.getToken().type()) {
                    case BOOLLITERAL -> code.loadConstant(Boolean.parseBoolean(exp.getToken().text()) ? 1 : 0);
                    case BINARYLITERAL, OCTLITERAL, HEXLITERAL, INTLITERAL -> {
                        var literal = exp.getToken().text().replace("_", "");
                        var radix = LiteralParselet.radix(literal);
                        var type = LiteralParselet.intTypeOf(literal);
                        literal = LiteralParselet.stripQualifier(LiteralParselet.stripRadixQualifier(literal));
                        switch (type.bitSize()){
                            case 8 ->  code.loadConstant(Byte.parseByte(literal, radix)).i2b();
                            case 16 ->  code.loadConstant(Short.parseShort(literal, radix)).i2s();
                            case 32 ->  code.loadConstant(Integer.parseInt(literal, radix));
                            case 64 ->  code.loadConstant(Long.parseLong(literal, radix));
                        }
                    }
                    case FLOATLITERAL -> {
                        var stripped = LiteralParselet.stripQualifier(exp.getToken().text());
                        switch (LiteralParselet.floatTypeOf(exp.getToken().text()).bitSize()){
                            case 32 -> code.loadConstant(Float.parseFloat(stripped));
                            case 64 -> code.loadConstant(Double.parseDouble(LiteralParselet.stripQualifier(exp.getToken().text())));
                        }
                    }
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
                    genExpression(code, state, child);
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

                    genExpression(code, state, key);
                    // Promote primitives
                    genConversion(code, BuiltinTypes.ANY, key.getResultType());

                    genExpression(code, state, value);
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
                                markLineNumber(code, lhs);
                                genExpression(code, state, lhs.getLeft());
                                String attr = lhs.getRight().getToken().text();

                                genExpression(code, state, exp.getRight());

                                // TODO - support statically resolved field access
                                //debugPrintTOS(code, "Setting field " + attr);
                                genConversion(code,  BuiltinTypes.ANY, exp.getRight().getResultType());
                                generateDynamicFieldAccess(code, attr, true);
                                return;
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
                            exp.getRight().visit(node -> genExpression(code, state, node));
                            // TODO - determine when we're reading a method binding
                            // If the parent is a block, do a "statement assignment" where we skip the dup() & pop() pair
                            // that would be otherwise necessary to support assignment as both an expression and statement. Unnecessary
                            // dup()/pop() can heavily impact performance, probably by causing the JIT's optimizer to miss otherwise
                            // available optimizations.
                            var assignType = exp.getParent().getNodeType().isBlock() ? LocalAccess.ASSIGN : LocalAccess.DUP_ASSIGN;
                            emitLocalReference(code, state, lhs.getToken().text(), exp.getRight().getResultType(), assignType, false);
                            return;
                        }
                    }
                    case LPAREN -> {
                        // This is of the form a.b() - do a callAt
                        if(exp.getLeft().is(NodeType.OPERATOR)
                                && exp.getLeft().getToken().type() == TokenType.DOT
                                && exp.getLeft().getRight().is(NodeType.ID)){

                            var dotOp = exp.getLeft();
                            // this is a dot access, so issue a callAt opcode
                            var callID = dotOp.getRight();


                            var dispatchTypes = new ArrayList<ObjectType>();
                            // Evaluate a
                            dotOp.getLeft().visit(node -> {
                                genExpression(code, state, node);
                                dispatchTypes.add(node.getResultType());
                            });
                            // Evaluate args
                            exp.visitChildren(node -> {
                                genExpression(code, state, node);
                                dispatchTypes.add(node.getResultType());
                            }, 1);

                            markLineNumber(code, exp);

                            var methodName = callID.getToken().text();
                            // Try to statically resolve the call. If we can't, emit a dynamic call.
                            // TODO - static call resolution

                            genDynamicInvocation(code, methodName, dispatchTypes.toArray(ObjectType[]::new));
                        }else{
                            var dispatchTypes = new ArrayList<ObjectType>();
                            exp.visitChildren(node -> {
                                genExpression(code, state, node);
                                dispatchTypes.add(node.getResultType());
                            });
                            markLineNumber(code, exp);
                            // Emit a()
                            genDynamicInvocation(code, "call", dispatchTypes.toArray(ObjectType[]::new));
                        }
                        return;
                    }
                    case DOT -> {
                        markLineNumber(code, exp.getLeft());
                        genExpression(code, state, exp.getLeft());

                        markLineNumber(code, exp);
                        String attr = exp.getRight().getToken().text();

                        // TODO - support statically resolved field access
                        generateDynamicFieldAccess(code, attr, false);
                        return;
                    }
                    case DOUBLEDOT -> {
                        genExpression(code, state, exp.getLeft());
                        genConversion(code, BuiltinTypes.ANY, exp.getLeft().getResultType());
                        genExpression(code, state, exp.getRight());
                        genConversion(code, BuiltinTypes.ANY, exp.getRight().getResultType());
                        code.loadConstant(1);
                        genConversion(code, BuiltinTypes.ANY, BuiltinTypes.BOOLEAN);
                        genDynamicInvocation(code, "range", exp.getLeft().getResultType(), exp.getRight().getResultType(), BuiltinTypes.BOOLEAN);
                        return;
                    }
                    case DOUBLEDOTLESS -> {
                        genExpression(code, state, exp.getLeft());
                        //genConversion(code, BuiltinTypes.ANY, exp.getLeft().getResultType());
                        genExpression(code, state, exp.getRight());
                        //genConversion(code, BuiltinTypes.ANY, exp.getRight().getResultType());
                        code.loadConstant(0);
                        //genConversion(code, BuiltinTypes.ANY, BuiltinTypes.BOOLEAN);
                        genDynamicInvocation(code, "range", exp.getLeft().getResultType(), exp.getRight().getResultType(), BuiltinTypes.BOOLEAN);
                        return;
                    }
                    case DOUBLECOLON -> {
                        genExpression(code, state, exp.getLeft());
                        if(exp.getRight().is(NodeType.ID)){
                            markLineNumber(code, exp);
                            code.loadConstant(exp.getRight().getToken().text());
                            code.invokestatic(descriptorFor(CRuntime.class), "bind",
                                    MethodTypeDesc.of(descriptorFor(MethodBinding.class), CD_Object, CD_String));
                            return;
                        }else{
                            throw new SyntaxError("Binding node operator requires a compile-time static method name");
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
                                genExpression(code, state, operand);
                                // Since this is an intrinsic we know the types are convertible
                                genConversion(code, emitter.op().pValues()[i], operandTypes[i]);
                            }

                            emitter.emitter().accept(code);
                        }, () -> {

                            for(int i = 0; i < operands.size(); i++){
                                var operand = operands.get(i);
                                genExpression(code, state, operand);
                                // This is a dynamic call, so box any primitives
                                // TODO - dynamic calls with primitives
                                //genConversion(code, BuiltinTypes.ANY, operandTypes[i]);
                            }

                            genDynamicInvocation(code, emitOp, operandTypes);
                        });
            }
        }
    }

    private void genDynamicInvocation(CodeBuilder code, String op, ClassDesc... argTypes){
        var objType = ClassDesc.of(Object.class.getName());

        var dynamicOp = binaryOpNames(op);

        var callType = MethodTypeDesc.of(objType, argTypes);

        var CD_Binder = ClassDesc.of(Binder.class.getName());
        var CD_CallSite = ClassDesc.of(CallSite.class.getName());
        var CD_MHLookup = ClassDesc.of(MethodHandles.Lookup.class.getName());
        var CD_MType = ClassDesc.of(MethodType.class.getName());
        var bootstrapDescriptor = MethodTypeDesc.of(CD_CallSite, CD_MHLookup, CD_String, CD_MType).descriptorString();

        System.out.println("Dynamic call to " + op + ": " + callType);
        code.invokedynamic(DynamicCallSiteDesc.of(
                MethodHandleDesc.of(DirectMethodHandleDesc.Kind.STATIC, CD_Binder,
                        Binder.INDY_BOOTSTRAP_METHOD, bootstrapDescriptor), dynamicOp, callType));
    }

    private void genDynamicInvocation(CodeBuilder code, String op, ObjectType... argTypes){
        var pTypes = Arrays.stream(argTypes)
                .map(this::descriptorFor)
                .toArray(ClassDesc[]::new);
        genDynamicInvocation(code, op, pTypes);
    }

    private void generateDynamicFieldAccess(CodeBuilder code, String field, boolean set) {
        var objType = ClassDesc.of(Object.class.getName());
        ClassDesc[] pTypes = new ClassDesc[set ? 2 : 1];
        pTypes[0] = objType;
        if(set){
            pTypes[1] = objType;
        }

        var callType = MethodTypeDesc.of(objType, pTypes);

        var CD_Binder = ClassDesc.of(Binder.class.getName());
        var CD_CallSite = ClassDesc.of(CallSite.class.getName());
        var CD_MHLookup = ClassDesc.of(MethodHandles.Lookup.class.getName());
        var CD_MType = ClassDesc.of(MethodType.class.getName());
        var bootstrapDescriptor = MethodTypeDesc.of(CD_CallSite, CD_MHLookup, CD_String, CD_MType).descriptorString();

        System.out.println("Dynamic field " + (set ? "set" : "get") + " to " + field);
        code.invokedynamic(DynamicCallSiteDesc.of(
                MethodHandleDesc.of(DirectMethodHandleDesc.Kind.STATIC, CD_Binder,
                        set ? Binder.INDY_BOOTSTRAP_SET : Binder.INDY_BOOTSTRAP_GET, bootstrapDescriptor), field, callType));

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

    private void genLocalVarDec(CodeBuilder code, CompilerState state, AstNode dec){
        // Note: after rewrites, a var dec will always have an assignment expression
        var type = BuiltinTypes.ANY;
        if(VarDec.hasAssignment(dec)){
            genExpression(code, state, VarDec.getAssignment(dec));
            type = dec.getResultType();
        }else{
            code.aconst_null();
        }
        emitLocalReference(code, state, dec.getSymbol().getName(), type, LocalAccess.ASSIGN, false);
    }

    private void genFlowControl(CodeBuilder code, CompilerState state, AstNode f){
        switch (f.getToken().type()) {
            case RETURN -> genReturn(code, state, f);
            case BREAK -> code.goto_(state.breakLabel());
            case CONTINUE -> code.goto_(state.continueLabel());
            // TODO - throw
        }
    }

    private void genBranch(CodeBuilder code, CompilerState state, AstNode guard, Label escape){
        if(guard.is(NodeType.OPERATOR)){
            // TODO - support unary branch intrinsics
            // TODO - support appropriate conversions for mixed types
            var lType = guard.getLeft().getResultType();
            var rType = guard.getRight().getResultType();
            if(rType.isAssignableTo(lType)){
                var emitter = Intrinsics.getBranch(guard.getToken().text(), lType);
                if(emitter.isPresent()){
                    genExpression(code, state, guard.getLeft());
                    genExpression(code, state, guard.getRight());
                    emitter.get().emitter().accept(code, escape);
                    return;
                }
            }
            // Fallback to generic branch
            genExpression(code, state, guard);

            genConversion(code, BuiltinTypes.BOOLEAN, guard.getResultType());
            code.ifeq(escape);
        }
    }

    private void genWhileLoop(CodeBuilder code, CompilerState state, AstNode loop){
        state.enterScope(loop.getSymbolTable());
        code.block(block -> {
            state.enterLoop(block.startLabel(), block.breakLabel());
            var guard = loop.getChild(0);
            genBranch(code, state, guard, block.breakLabel());
            loop.visitChildren(node -> genStatement(code, state, node), 1);
            code.goto_(block.startLabel());
            state.exitLoop();
        });
        state.exitScope();
    }

    private void genIfElse(CodeBuilder code, CompilerState state, AstNode ifElse){
        // If-else consists of an IF_ELSE parent node with 1+ children.
        // The first child is always an if branch, possibly followed by more if-branches and possibly
        // followed at the end by an else-branch.

        genIfBranches(code, state, ifElse.getChildren(), 0);
    }

    private void genIfBranches(CodeBuilder code, CompilerState state, List<AstNode> branches, int i){
        var branch = branches.get(i);
        if(branch.is(NodeType.IF)){
            code.block(block -> {
                genBranch(code, state, branch.getLeft(), block.breakLabel());
                branch.visitChildren(node -> genStatement(block, state, node), 1);
            });
            if(i < branches.size() - 1){
                code.block(
                    block -> genIfBranches(block, state, branches, i + 1));
            }
        }else{
            // Generating an else block
            code.block(block -> branch.visitChildren(node -> genStatement(block, state, node)));
        }
    }

    private void genForLoop(CodeBuilder code, CompilerState state, AstNode loop){
        // Get the iterator
        var symbols = loop.getSymbolTable();
        state.enterScope(symbols);
        var iter = loop.getChild();
        var iterName = iter.getSymbol();
        var iterLocal = symbols.getLocalIndex(iterName);

        var id = iter.getLeft();
        var idName = id.getSymbol();
        var idLocal = symbols.getLocalIndex(idName);

        // Create and store the iterator before entering the loop
        genExpression(code, state, iter.getRight());
        // TODO - dynamic invocation support
        code.invokeinterface(descriptorFor(Iterable.class), "iterator", MethodTypeDesc.of(ClassDesc.of(Iterator.class.getName())));
        code.storeLocal(TypeKind.REFERENCE, iterLocal);

        code.block(block -> {
            // TODO - handle dynamic invocation & truthiness conversion if needed
            // 'hasNext' guards the loop
            state.enterLoop(block.startLabel(), block.breakLabel());
            code.loadLocal(TypeKind.REFERENCE, iterLocal);
            code.invokeinterface(descriptorFor(Iterator.class), "hasNext", MethodTypeDesc.of(CD_boolean));
            // TODO - support dynamic dispatch here
            code.ifeq(block.breakLabel());

            // First task is calling the iterator and storing the result
            code.loadLocal(TypeKind.REFERENCE, iterLocal);
            code.invokeinterface(descriptorFor(Iterator.class), "next", MethodTypeDesc.of(CD_Object)); // TODO - actual type
            code.storeLocal(TypeKind.REFERENCE, idLocal);

            loop.visitChildren(node -> genStatement(code, state, node), 1);

            code.goto_(block.startLabel());
            state.exitLoop();
        });
        state.exitScope();
    }

    private void genReturn(CodeBuilder code, CompilerState state, AstNode rNode){
        if(rNode.hasChildren()){
            genExpression(code, state, rNode.getChild());
        }else{
            code.aconst_null();
        }

        var type = rNode.getResultType();
        if(type == null){
            type = BuiltinTypes.ANY;
        }

        var expectedType = getExpectedReturnType(rNode);
        markLineNumber(code, rNode);

        genReturn(code, expectedType, type);
    }

    private void genReturn(CodeBuilder code, ObjectType expectedType, ObjectType type){
        if(!type.isAssignableTo(expectedType)){
            promoteType(code, expectedType, type);
        }
        var generator = returnGenerators.get(type);
        generator.accept(code);
    }

    private void promoteType(CodeBuilder code, ObjectType expected, ObjectType actual){
        var converter = Intrinsics.getConversion(expected, actual)
                .orElseThrow(() -> new IllegalArgumentException("Cannot convert from " + expected.name() + " to " + actual.name() + ". This is a compiler bug."));
        converter.emitter().accept(code);
    }

    protected ObjectType getExpectedReturnType(AstNode rNode){
        var parent = rNode.getParent();
        while(!parent.is(NodeType.METHOD)){
            parent = parent.getParent();
        }
        var rType = parent.getResultType();
        return rType != null ? rType : BuiltinTypes.ANY;
    }

    private void emitLocalReference(CodeBuilder code, CompilerState state, String name, ObjectType localType, LocalAccess assign, boolean bindingRead){
        // TODO - we shouldn't allow undefined types at program emit time.
        if(localType == null){
            localType = BuiltinTypes.ANY;
        }

        Deque<SymbolTable> trace = state.getSymbolTrace(name);

        if(trace == null){
            throw new IllegalStateException(name + " not found");
        }

        SymbolTable table = trace.getLast();
        var symbol = table.getSymbol(name);
        var localIndex = table.getLocalIndex(symbol);

        if(localIndex == -1){
            throw new IllegalStateException(name + " is not a local. This is a compiler bug.");
        }

        if(assign == LocalAccess.LOAD){
            if(symbol.isUpvalueRef() || (symbol.isUpvalue() && !bindingRead)){
                //assembler.getUpvalue(localIndex);
            }else{
                code.loadLocal(typeKind(localType), localIndex);
            }
        }else{
            if(assign == LocalAccess.DUP_ASSIGN){
                code.dup();
            }
            if(symbol.isUpvalueRef() || symbol.isUpvalue()){
                //assembler.setUpvalue(localIndex);
            }else{
                var storageType = symbol.getReferentType();
                if(!localType.isAssignableTo(storageType)){
                    genConversion(code, storageType, localType);
                }
                code.storeLocal(typeKind(storageType), localIndex);
            }
        }
    }

    private TypeKind typeKind(ObjectType t){
        return switch (t){
            case BooleanType _ -> TypeKind.BOOLEAN;
            case IntegerType i ->
                    switch (i.bitSize()){
                        case 8 -> TypeKind.BYTE;
                        case 16 -> TypeKind.SHORT;
                        case 32 -> TypeKind.INT;
                        case 64 -> TypeKind.LONG;
                        default -> TypeKind.LONG;
                    };
            case FloatType f ->
                    switch (f.bitSize()){
                        case 32 -> TypeKind.FLOAT;
                        case 64 -> TypeKind.DOUBLE;
                        default -> TypeKind.DOUBLE;
                    };
            default -> TypeKind.REFERENCE;
        };
    }

    private String binaryOpNames(String op){
        return switch(op){
            case "+" -> "plus";
            case "-" -> "minus";
            case "*" -> "mul";
            case "/" -> "div";
            case "//" -> "fdiv";
            case "%" -> "mod";
            case "pow" -> "mul";
            case "^" -> "binaryXor";
            case "&" -> "binaryAnd";
            case "|" -> "binaryOr";
            case "<<" -> "lShift";
            case ">>" -> "rShift";
            case ">>>" -> "unsignedRShift";
            case "==" -> "equals";
            case "<" -> "compare";
            case "<=" -> "compare";
            case ">" -> "compare";
            case ">=" -> "compare";
            default -> op;
        };
    }

    private void markLineNumber(CodeBuilder code, AstNode n){
        if(n.getLineNumber() != -1){
            code.lineNumber(n.getLineNumber());
        }
    }

    public Class<?> bindingFor(ChipmunkClassLoader loader, String bindingName, Class<?> targetType, String methodName){

        var bindingClassDesc = ClassDesc.of(bindingName);
        var compiledClass = ClassFile.of().build(bindingClassDesc, cls -> {
            cls.with(RuntimeVisibleAnnotationsAttribute.of(Annotation.of(descriptorFor(AllowChipmunkLinkage.class))));
            cls.withSuperclass(descriptorFor(MethodBinding.class));

            cls.withMethodBody("<init>", MethodTypeDesc.of(CD_void, CD_Object, CD_String), ClassFile.ACC_PUBLIC, init -> {
                init.aload(0)
                        .aload(1)
                        .aload(2)
                        .invokespecial(descriptorFor(MethodBinding.class), "<init>", MethodTypeDesc.of(CD_void, CD_Object, CD_String))
                        .return_();
            });

            var methods = Arrays.stream(targetType.getMethods())
                    .filter(m -> m.getName().equals(methodName))
                    .toList();

            for(var method : methods){
                var pDescs = Arrays.stream(method.getParameterTypes())
                        .map(this::descriptorFor)
                        .toArray(ClassDesc[]::new);

                var methodDesc = MethodTypeDesc.of(descriptorFor(method.getReturnType()), pDescs);

                cls.withMethodBody("call", methodDesc, ClassFile.ACC_PUBLIC, code -> {
                    code.aload(0)
                            .getfield(bindingClassDesc, MethodBinding.TARGET_FIELD_NAME, CD_Object)
                            .checkcast(ClassDesc.of(targetType.getName()));

                    for(int i = 1; i <= method.getParameterCount(); i++){
                        code.aload(i);
                    }

                    genDynamicInvocation(code, methodName, Stream.concat(Stream.of(descriptorFor(targetType)), Stream.of(pDescs)).toArray(ClassDesc[]::new));
                    var rType = method.getReturnType();
                    // TODO - support non-boxed primitive returns from target
                    if(rType.equals(boolean.class)){
                        code.checkcast(ClassDesc.of(Boolean.class.getName()));
                        code.invokevirtual(ClassDesc.of(Boolean.class.getName()), "booleanValue", MethodTypeDesc.of(CD_boolean));
                        code.ireturn();
                    }else if(rType.equals(byte.class)){
                        code.checkcast(ClassDesc.of(Byte.class.getName()));
                        code.invokevirtual(ClassDesc.of(Byte.class.getName()), "byteValue", MethodTypeDesc.of(CD_byte));
                        code.ireturn();
                    }else if(rType.equals(short.class)){
                        code.checkcast(ClassDesc.of(Short.class.getName()));
                        code.invokevirtual(ClassDesc.of(Short.class.getName()), "shortValue", MethodTypeDesc.of(CD_short));
                        code.ireturn();
                    }else if(rType.equals(int.class)){
                        code.checkcast(ClassDesc.of(Integer.class.getName()));
                        code.invokevirtual(ClassDesc.of(Integer.class.getName()), "intValue", MethodTypeDesc.of(CD_int));
                        code.ireturn();
                    }else if(rType.equals(long.class)){
                        code.checkcast(ClassDesc.of(Long.class.getName()));
                        code.invokevirtual(ClassDesc.of(Long.class.getName()), "longValue", MethodTypeDesc.of(CD_long));
                        code.lreturn();
                    }else if(rType.equals(float.class)){
                        code.freturn();
                    }else if(rType.equals(double.class)){
                        code.dreturn();
                    }else{
                        if(rType.equals(void.class)){
                            code.aconst_null();
                        }
                        code.areturn();
                    }
                });
            }

        });

        return loader.define(bindingName, compiledClass);
    }

    private void debugPrint(CodeBuilder b, String msg){
        b.loadConstant(msg)
                .invokestatic(ClassDesc.of(CRuntime.class.getName()), "print", MethodTypeDesc.of(CD_void, CD_String));
    }

    private void debugPrintTOS(CodeBuilder b, String msg){
        b.dup().loadConstant(msg)
                .invokestatic(ClassDesc.of(CRuntime.class.getName()), "print", MethodTypeDesc.of(CD_void, CD_Object, CD_String));
    }

}
