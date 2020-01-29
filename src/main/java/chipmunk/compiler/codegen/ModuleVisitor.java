package chipmunk.compiler.codegen;

import java.util.ArrayList;
import java.util.List;

import chipmunk.DebugEntry;
import chipmunk.ExceptionBlock;
import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.ClassNode;
import chipmunk.compiler.ast.ImportNode;
import chipmunk.compiler.ast.MethodNode;
import chipmunk.compiler.ast.ModuleNode;
import chipmunk.compiler.ast.VarDecNode;
import chipmunk.modules.runtime.CClass;
import chipmunk.modules.runtime.CMethod;
import chipmunk.modules.runtime.CModule;
import chipmunk.modules.runtime.CNull;

public class ModuleVisitor implements AstVisitor {
	
	protected CModule module;
	protected Codegen initCodegen;
	protected ChipmunkAssembler assembler;
	protected List<Object> constantPool;
	
	public ModuleVisitor(){
		constantPool = new ArrayList<Object>();
		assembler = new ChipmunkAssembler(constantPool);
	}

	@Override
	public void visit(AstNode node) {
		if(node instanceof ModuleNode){
			
			ModuleNode moduleNode = (ModuleNode) node;
			module = new CModule(moduleNode.getSymbol().getName(), constantPool);
			initCodegen = new Codegen(assembler, moduleNode.getSymbolTable(), module);
			moduleNode.visitChildren(this);
			
		}else if(node instanceof ClassNode){
			
			ClassVisitor visitor = new ClassVisitor(constantPool, module);
			node.visit(visitor);
			
			CClass cClass = visitor.getCClass();
			
			// generate initialization code to run class initializer
			if(cClass.getSharedInitializer() != null){
				ChipmunkAssembler initAssembler = initCodegen.getAssembler();
				
				initAssembler.getModule(cClass.getName());
				initAssembler.init();
				initAssembler.call((byte)0);
				initAssembler.pop();
			}
			
			module.getNamespace().set(cClass.getName(), cClass);
			
		}else if(node instanceof MethodNode){
			
			MethodVisitor visitor = new MethodVisitor(constantPool, module);
			node.visit(visitor);
			CMethod method = visitor.getMethod();
			
			method.bind(module);
			
			module.getNamespace().set(visitor.getMethodSymbol().getName(), method);
			
		}else if(node instanceof ImportNode){
			
			ImportNode importNode = (ImportNode) node;
			boolean importAll = importNode.isImportAll();
			
			CModule.Import im = module.new Import(importNode.getModule(), importAll);
			
			if(!importAll){
				im.getSymbols().addAll(importNode.getSymbols());
				im.getAliases().addAll(importNode.getAliases());
			}
			
			module.getImports().add(im);
		}else if(node instanceof VarDecNode){
			
			// TODO - final variables
			VarDecNode varDec = (VarDecNode) node;
			
			VarDecVisitor visitor = new VarDecVisitor(initCodegen);
			visitor.visit(varDec);
			
			module.getNamespace().set(varDec.getVarName(), CNull.instance());
		}else{
			throw new IllegalArgumentException("Error parsing module " + module.getName() + ": illegal AST node type " + node.getClass());
		}
	}
	
	public CModule getModule(){
		
		CMethod initializer = new CMethod();
		
		assembler.pushNull();
		assembler._return();

		initializer.getCode().setConstantPool(assembler.getConstantPool().toArray());
		initializer.getCode().setCode(assembler.getCodeSegment());
		initializer.getCode().setLocalCount(0);
		initializer.getCode().setExceptionTable(initCodegen.getExceptionBlocks().toArray(new ExceptionBlock[]{}));
		initializer.getCode().setDebugTable(initCodegen.getAssembler().getDebugTable().toArray(new DebugEntry[]{}));
		initializer.getCode().setDebugSymbol(module.getName() + ".<module init>");
		
		initializer.bind(module);
		initializer.getCode().setModule(module);
		module.setInitializer(initializer);
		
		return module;
	}

}
