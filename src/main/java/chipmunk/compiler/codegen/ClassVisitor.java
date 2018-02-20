package chipmunk.compiler.codegen;

import java.util.ArrayList;
import java.util.List;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.ClassNode;
import chipmunk.compiler.ast.MethodNode;
import chipmunk.compiler.ast.VarDecNode;
import chipmunk.modules.reflectiveruntime.CClass;
import chipmunk.modules.reflectiveruntime.CMethod;
import chipmunk.modules.reflectiveruntime.CModule;
import chipmunk.modules.reflectiveruntime.CNull;

public class ClassVisitor implements AstVisitor {

	protected CClass cClass;
	protected List<Object> constantPool;
	
	protected Codegen sharedInitCodegen;
	protected Codegen instanceInitCodegen;
	
	protected ChipmunkAssembler sharedInitAssembler;
	protected ChipmunkAssembler instanceInitAssembler;
	
	protected CModule module;
	
	public ClassVisitor(CModule module){
		this(new ArrayList<Object>(), module);
	}
	
	public ClassVisitor(List<Object> constantPool, CModule module){
		this.constantPool = constantPool;
		this.module = module;
	}
	
	@Override
	public void visit(AstNode node) {
		
		if(node instanceof ClassNode){
			ClassNode classNode = (ClassNode) node;
			
			sharedInitAssembler = new ChipmunkAssembler(constantPool);
			instanceInitAssembler = new ChipmunkAssembler(constantPool);

			sharedInitCodegen = new Codegen(sharedInitAssembler, classNode.getSymbolTable());
			instanceInitCodegen = new Codegen(instanceInitAssembler, classNode.getSymbolTable());
			
			cClass = new CClass(classNode.getName(), module);
			
			// TODO - inheritance
			classNode.visitChildren(this);
			
		}else if(node instanceof VarDecNode){
			// TODO - final variables
			VarDecNode varDec = (VarDecNode) node;
			
			VarDecVisitor visitor = null;
			final boolean isShared = varDec.getSymbol().isShared();
			
			if(isShared){
				visitor = new VarDecVisitor(sharedInitCodegen);
			}else{
				visitor = new VarDecVisitor(instanceInitCodegen);
			}
			
			visitor.visit(varDec);
			
			if(isShared){
				cClass.getAttributes().set(varDec.getVarName(), new CNull());
			}else{
				cClass.getInstanceAttributes().set(varDec.getVarName(), new CNull());
			}
			
		}else if(node instanceof MethodNode){
			MethodNode methodNode = (MethodNode) node;
			
			MethodVisitor visitor = new MethodVisitor(constantPool);
			methodNode.visit(visitor);
				
			CMethod method = visitor.getMethod();
			
			method.bind(cClass);
			method.setModule(module);
			
			if(methodNode.getSymbol().isShared()){
				cClass.getAttributes().set(methodNode.getName(), method);
			}else{
				cClass.getInstanceAttributes().set(methodNode.getName(), method);
			}
		}
		
		return;
	}
	
	public CClass getCClass(){
		
		CMethod sharedInitializer = new CMethod();
		
		sharedInitAssembler.pushNull();
		sharedInitAssembler._return();
		
		sharedInitializer.setConstantPool(sharedInitAssembler.getConstantPool());
		sharedInitializer.setCode(sharedInitAssembler.getCodeSegment());
		sharedInitializer.setLocalCount(0);
		
		sharedInitializer.bind(cClass);
		sharedInitializer.setModule(module);
		
		cClass.setSharedInitializer(sharedInitializer);
		
		CMethod instanceInitializer = new CMethod();
		
		instanceInitAssembler.pushNull();
		instanceInitAssembler._return();
		
		instanceInitializer.setConstantPool(instanceInitAssembler.getConstantPool());
		instanceInitializer.setCode(instanceInitAssembler.getCodeSegment());
		instanceInitializer.setLocalCount(0);
		
		instanceInitializer.bind(cClass);
		instanceInitializer.setModule(module);
		
		cClass.setInstanceInitializer(instanceInitializer);
		
		return cClass;
	}

}
