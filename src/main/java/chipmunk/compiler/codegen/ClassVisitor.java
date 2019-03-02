package chipmunk.compiler.codegen;

import java.util.ArrayList;
import java.util.List;

import chipmunk.Namespace;
import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.ClassNode;
import chipmunk.compiler.ast.MethodNode;
import chipmunk.compiler.ast.VarDecNode;
import chipmunk.modules.runtime.CClass;
import chipmunk.modules.runtime.CMethod;
import chipmunk.modules.runtime.CModule;
import chipmunk.modules.runtime.CNull;

public class ClassVisitor implements AstVisitor {

	protected CClass cClass;
	protected List<Object> constantPool;
	
	protected Codegen sharedInitCodegen;
	protected Codegen instanceInitCodegen;
	
	protected ChipmunkAssembler sharedInitAssembler;
	protected ChipmunkAssembler instanceInitAssembler;
	
	protected CModule module;
	
	private boolean alreadyReachedConstructor;
	private ChipmunkAssembler expAssembler;
	
	public ClassVisitor(CModule module){
		this(new ArrayList<Object>(), module);
	}
	
	public ClassVisitor(List<Object> constantPool, CModule module){
		this(constantPool, module, null);
	}
	
	public ClassVisitor(List<Object> constantPool, CModule module, ChipmunkAssembler expAssembler){
		this.constantPool = constantPool;
		this.module = module;
		alreadyReachedConstructor = false;
		this.expAssembler = expAssembler;
		
	}
	
	@Override
	public void visit(AstNode node) {
		
		if(node instanceof ClassNode){
			ClassNode classNode = (ClassNode) node;
			
			sharedInitAssembler = new ChipmunkAssembler(constantPool);
			instanceInitAssembler = new ChipmunkAssembler(constantPool);

			sharedInitCodegen = new Codegen(sharedInitAssembler, classNode.getSymbolTable(), module);
			instanceInitCodegen = new Codegen(instanceInitAssembler, classNode.getSymbolTable(), module);
			
			if(cClass == null) {
				cClass = new CClass(classNode.getName(), module);
				classNode.visitChildren(this);
			}else {
				// visit nested class declarations
				ClassVisitor visitor = new ClassVisitor(constantPool, module);
				classNode.visit(visitor);
				CClass inner = visitor.getCClass();
				
				if(classNode.getSymbol().isShared()) {
					cClass.getAttributes().set(inner.getName(), inner);
				}else {
					cClass.getInstanceAttributes().set(inner.getName(), inner);
				}
			}
			
			//if(expAssembler != null) {
			//	expAssembler.push(cClass);
			//}
			
			
		}else if(node instanceof VarDecNode){
			// TODO - final variables
			VarDecNode varDec = (VarDecNode) node;
			
			VarDecVisitor visitor = null;
			final boolean isShared = varDec.getSymbol().isShared();
			final boolean isFinal = varDec.getSymbol().isFinal();
			final boolean isTrait = varDec.getSymbol().isTrait();
			
			if(isShared){
				visitor = new VarDecVisitor(sharedInitCodegen);
			}else{
				visitor = new VarDecVisitor(instanceInitCodegen);
			}
			
			visitor.visit(varDec);
			
			Namespace clsNamespace;
			if(isShared){
				clsNamespace = cClass.getAttributes();
			}else{
				clsNamespace = cClass.getInstanceAttributes();
			}
			
			if(isTrait) {
				clsNamespace.setTrait(varDec.getVarName(), CNull.instance());
			}else {
				clsNamespace.set(varDec.getVarName(), CNull.instance());
			}
			
		}else if(node instanceof MethodNode){
			MethodNode methodNode = (MethodNode) node;
			
			
			MethodVisitor visitor = null;
			
			// this is the constructor
			if(methodNode.getSymbol().getName().equals(cClass.getName())){
				if(alreadyReachedConstructor){
					// TODO - throw error until we have support for multi-methods
					throw new IllegalStateException("Only one constructor per class allowed");
				}
				alreadyReachedConstructor = true;
				
				ChipmunkAssembler assembler = new ChipmunkAssembler(constantPool);
				
				// call instance initializer before doing anything else
				genInitCall(assembler);
				
				visitor = new MethodVisitor(assembler, module);
				visitor.setDefaultReturn(false);
				methodNode.visit(visitor);

				// return self
				visitor.genSelfReturn();
				
			}else{
				// regular method, use shared constant pool
				visitor = new MethodVisitor(constantPool, module);
				methodNode.visit(visitor);
			}
			
				
			CMethod method = visitor.getMethod();
			
			if(methodNode.getSymbol().isShared()){
				method.bind(cClass);
				cClass.getAttributes().set(methodNode.getName(), method);
			}else{
				cClass.getInstanceAttributes().set(methodNode.getName(), method);
			}
		}
		
		return;
	}
	
	public CClass getCClass(){
		
		CMethod sharedInitializer = new CMethod();
		
		sharedInitAssembler.getLocal(0);
		sharedInitAssembler._return();
		
		sharedInitializer.setConstantPool(sharedInitAssembler.getConstantPool().toArray());
		sharedInitializer.setCode(sharedInitAssembler.getCodeSegment());
		sharedInitializer.setLocalCount(1);
		
		sharedInitializer.bind(cClass);
		sharedInitializer.setModule(module);
		
		cClass.setSharedInitializer(sharedInitializer);
		
		CMethod instanceInitializer = new CMethod();
		
		// return newly created instance
		instanceInitAssembler.getLocal(0);
		instanceInitAssembler._return();
		
		instanceInitializer.setConstantPool(instanceInitAssembler.getConstantPool().toArray());
		instanceInitializer.setCode(instanceInitAssembler.getCodeSegment());
		instanceInitializer.setLocalCount(1);
		
		instanceInitializer.setModule(module);
		// instance initializer will be bound at runtime to newly created
		// instances
		
		cClass.setInstanceInitializer(instanceInitializer);
		
		// generate default constructor if no constructor was specified
		if(!alreadyReachedConstructor){
			ChipmunkAssembler assembler = new ChipmunkAssembler(constantPool);
			genInitCall(assembler);
			// return self
			assembler.getLocal(0);
			assembler._return();
			
			CMethod constructor = new CMethod();
			constructor.setArgCount(0);
			constructor.setLocalCount(1);
			constructor.setConstantPool(constantPool.toArray());
			constructor.setModule(module);
			constructor.setCode(assembler.getCodeSegment());
			
			cClass.getInstanceAttributes().set(cClass.getName(), constructor);
		}
		
		return cClass;
	}
	
	private void genInitCall(ChipmunkAssembler assembler){
		assembler.getLocal(0);
		assembler.init();
		assembler.call((byte)0);
	}

}
