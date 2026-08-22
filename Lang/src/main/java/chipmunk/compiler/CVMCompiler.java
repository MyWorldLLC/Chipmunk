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
import chipmunk.compiler.symbols.SymbolTable;
import chipmunk.compiler.types.*;
import chipmunk.modules.lang.LangModule;
import chipmunk.runtime.ChipmunkModule;
import chipmunk.vm.ModuleLoader;
import chipmunk.vm.invoke.Binder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.classfile.*;
import java.lang.constant.*;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AccessFlag;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
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
                new SymbolAccessRewriteVisitor(),
                new ConstructorVisitor(),
                new ImportResolverVisitor(Arrays.asList(astResolver, binaryResolver, nativeResolver)),
                new TypeInferenceVisitor(),
                new VarInitRewriteVisitor(),
                new InnerMethodRewriteVisitor()
        ); // TODO - type checking
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

    public List<ModuleClasses> compile(Compilation compilation) throws CompileChipmunk {
        var asts = new ArrayList<ParsedModule>();

        for(ChipmunkSource source : compilation.getSources()){
            List<AstNode> parsed = parse(lex(source.readFully()), source.getFileName());
            parsed.forEach(n -> asts.add(new ParsedModule(source.getFileName(), n)));
        }

        return compile(asts);
    }

    public List<ModuleClasses> compile(AstNode... asts) throws CompileChipmunk {
        return compile(Arrays.stream(asts).map(a -> new ParsedModule("<memory>", a)).toList());
    }

    public List<ModuleClasses> compile(ParsedModule... modules) throws CompileChipmunk {
        return compile(Arrays.asList(modules));
    }

    public List<ModuleClasses> compile(List<ParsedModule> parsedModules) throws CompileChipmunk {
        astResolver.setModules(parsedModules.stream().map(ParsedModule::ast).toList());

        parsedModules.forEach(p -> visitAst(p.ast(), passes));

        var modules = new ArrayList<ModuleClasses>();
        for(int i = 0; i < parsedModules.size(); i++){
            var parsed = parsedModules.get(i);
            var ast = parsed.ast();
            CompilerUtil.dumpTree(ast);
            var name = Modules.getName(ast).getName();
            // TODO - package-prefixed module class name?
            var code = generateCode(parsed);
            try {
                Files.write(Path.of("./" + name + ".class"), code.get(name));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            modules.add(new ModuleClasses(name, name, generateCode(parsed)));
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

        return compile(new ParsedModule("runtimeExpression", module)).getFirst().classes().get("exp");
    }

    public byte[] compileMethod(String methodDef) throws CompileChipmunk {
        AstNode module = Modules.make("exp");

        TokenStream tokens = lex(methodDef);
        ChipmunkParser parser = new ChipmunkParser(tokens);

        AstNode method = parser.parseMethodDef();

        module.addChild(method);

        return compile(new ParsedModule("runtimeMethod", module)).getFirst().classes().get("exp");
    }

    protected Map<String, byte[]> generateCode(ParsedModule module) throws CompileChipmunk {
        var classes = new HashMap<String, byte[]>();
        classes.put(Modules.getName(module.ast()).getName(), genClass(Modules.getName(module.ast()),
                cls -> {
                    cls.withInterfaceSymbols(ClassDesc.of(ChipmunkModule.class.getName()));
                    var ast = module.ast();

                    var state = new CompilerState();
                    state.enterScope(ast.getSymbolTable());

                    for(var element : ast.getChildren()){
                        switch (element.getNodeType()){
                            case VAR_DEC -> genVarDec(cls, state, element);
                            case METHOD -> genMethod(cls, state, element);
                            case CLASS -> {
                                var clsName = element.getSymbol();
                                classes.put(clsName.getName(), genClass(clsName, (clsBuilder) -> {
                                    // TODO
                                }));
                            }
                        }
                    }
                    state.exitScope();
                }));
        return classes;
    }

    private byte[] genClass(Symbol symbol, Consumer<ClassBuilder> builder){
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
            default -> {
                genExpression(code, state, statement);
                code.pop();
            }
        }
    }

    private void genVarDec(ClassBuilder cls, CompilerState state, AstNode varDec){
        var name = VarDec.getVarName(varDec);
        cls.withField(name, ClassDesc.of(Object.class.getName()),
                field -> {
                    var symbol = state.scope().getSymbol(name);
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

    private void genExpression(CodeBuilder code, CompilerState state, AstNode exp){
        markLineNumber(code, exp);
        switch(exp.getNodeType()){
            case ID -> {
                var symbol = state.scope().getSymbol(exp.getToken().text());
                emitLocalReference(code, state, symbol.getName(), symbol.getReferentType(), false, false);
            }
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
                            exp.getRight().visit(node -> genExpression(code, state, node));
                            // TODO - determine when we're reading a method binding
                            emitLocalReference(code, state, lhs.getToken().text(), exp.getRight().getResultType(), true, false);
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

                            // Evaluate a
                            dotOp.getLeft().visit(node -> genExpression(code, state, node));
                            // Evaluate args
                            exp.visitChildren(node -> genExpression(code, state, node), 1);

                            int argCount = exp.childCount() - 1;
                            markLineNumber(code, exp);

                            var methodName = callID.getToken().text();
                            // Try to statically resolve the call. If we can't, emit a dynamic call.
                            // TODO - static call resolution

                            System.out.println("Generating dynamic invocation for " + exp);
                            genDynamicInvocation(code, methodName, argCount + 1);
                            //assembler.callAt(callID.getToken().text(), (byte)argCount);

                        }else{
                            int argCount = exp.childCount() - 1;
                            exp.visitChildren(node -> genExpression(code, state, node));
                            markLineNumber(code, exp);
                            // Emit a()
                            // TODO
                            //assembler.call((byte) argCount);
                        }
                        return;
                    }
                    case DOT -> {
                        markLineNumber(code, exp.getLeft());
                        genExpression(code, state, exp.getLeft());
                        var attr = exp.getRight().getToken().text();
                        markLineNumber(code, exp);
                        System.out.println("Generating dynamic field access for " + exp);
                        generateDynamicFieldAccess(code, attr, false);
                        /*assembler.onLine(op.getLeft().getLineNumber());
                        op.getLeft().visit(this);
                        assembler.onLine(op.getLineNumber());

                        String attr = op.getRight().getToken().text();
                        assembler.getattr(attr);*/
                        return;
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

        var dynamicOp = binaryOpNames(op);

        var callType = MethodTypeDesc.of(objType, pTypes);

        var CD_Binder = ClassDesc.of(Binder.class.getName());
        var CD_CallSite = ClassDesc.of(CallSite.class.getName());
        var CD_MHLookup = ClassDesc.of(MethodHandles.Lookup.class.getName());
        var CD_MType = ClassDesc.of(MethodType.class.getName());
        var bootstrapDescriptor = MethodTypeDesc.of(CD_CallSite, CD_MHLookup, CD_String, CD_MType).descriptorString();
        System.out.println("Warning - emitting dynamic call to " + op + "(" + argc + ")");

        code.invokedynamic(DynamicCallSiteDesc.of(
                MethodHandleDesc.of(DirectMethodHandleDesc.Kind.STATIC, CD_Binder,
                        Binder.INDY_BOOTSTRAP_METHOD, bootstrapDescriptor), dynamicOp, callType));
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

        System.out.println("Warning - emitting dynamic field access to " + field + "(" + set + ")");

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
        emitLocalReference(code, state, dec.getSymbol().getName(), type, true, false);
    }

    private void genFlowControl(CodeBuilder code, CompilerState state, AstNode f){
        switch (f.getToken().type()) {
            case RETURN -> genReturn(code, state, f);
            case BREAK -> code.goto_(state.breakLabel());
            case CONTINUE -> code.goto_(state.continueLabel());
            // TODO - throw/break/continue
        }
    }

    private void genWhileLoop(CodeBuilder code, CompilerState state, AstNode loop){
        // TODO - suspension support
        state.enterScope(loop.getSymbolTable());
        code.block(block -> {
            state.enterLoop(block.startLabel(), block.breakLabel());
            var guard = loop.getChild(0);
            genExpression(code, state, guard);
            genConversion(code, BuiltinTypes.BOOLEAN, guard.getResultType());
            code.ifeq(block.breakLabel());
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
            genExpression(code, state, branch.getLeft());
            if(!branch.getLeft().getResultType().isAssignableTo(BuiltinTypes.BOOLEAN)){
                genConversion(code, BuiltinTypes.BOOLEAN, branch.getLeft().getResultType());
            }
            if(i < branches.size() - 1){
                code.ifThenElse(block -> branch.visitChildren(node -> genStatement(block, state, node)),
                        block -> genIfBranches(block, state, branches, i + 1));
            }else{
                code.ifThen(block -> branch.visitChildren(node -> genStatement(block, state, node)));
            }
        }else{
            // Generating an else block
            code.block(block -> branch.visitChildren(node -> genStatement(block, state, node)));
        }
    }

    private void genForLoop(CodeBuilder code, CompilerState state, AstNode loop){
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
        genExpression(code, state, iter.getRight());
        code.storeLocal(TypeKind.REFERENCE, iterLocal);

        code.block(block -> {
            // TODO - handle dynamic invocation & truthiness conversion if needed
            // 'hasNext' guards the loop
            state.enterLoop(block.startLabel(), block.breakLabel());
            code.loadLocal(TypeKind.REFERENCE, iterLocal);
            code.invokevirtual(descriptorFor(Iterator.class), "hasNext", MethodTypeDesc.of(CD_boolean));
            code.ifne(block.breakLabel());

            // First task is calling the iterator and storing the result
            code.loadLocal(TypeKind.REFERENCE, iterLocal);
            code.invokevirtual(descriptorFor(Iterator.class), "next", MethodTypeDesc.of(CD_Object)); // TODO - actual type
            code.storeLocal(TypeKind.REFERENCE, idLocal);

            loop.visitChildren(node -> genStatement(code, state, node), 1);

            code.goto_(block.startLabel());
            state.exitLoop();
        });
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
    }

    protected ObjectType getExpectedReturnType(AstNode rNode){
        var parent = rNode.getParent();
        while(!parent.is(NodeType.METHOD)){
            parent = parent.getParent();
        }
        var rType = parent.getResultType();
        return rType != null ? rType : BuiltinTypes.ANY;
    }

    private void emitLocalReference(CodeBuilder code, CompilerState state, String name, ObjectType localType, boolean assign, boolean bindingRead){
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

        if(assign){
            code.dup();
            if(symbol.isUpvalueRef() || symbol.isUpvalue()){
                //assembler.setUpvalue(localIndex);
            }else{
                var storageType = symbol.getReferentType();
                if(!localType.isAssignableTo(storageType)){
                    genConversion(code, storageType, localType);
                }
                code.storeLocal(typeKind(storageType), localIndex);
            }
        }else{
            if(symbol.isUpvalueRef() || (symbol.isUpvalue() && !bindingRead)){
                //assembler.getUpvalue(localIndex);
            }else{
                code.loadLocal(typeKind(localType), localIndex);
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

}
