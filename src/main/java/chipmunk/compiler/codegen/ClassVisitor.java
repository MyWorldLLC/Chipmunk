package chipmunk.compiler.codegen;

import java.util.ArrayList;
import java.util.List;

import chipmunk.ChipmunkDisassembler;
import chipmunk.DebugEntry;
import chipmunk.ExceptionBlock;
import chipmunk.Namespace;
import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.Token;
import chipmunk.compiler.ast.*;
import chipmunk.modules.runtime.CClass;
import chipmunk.modules.runtime.CMethod;
import chipmunk.modules.runtime.CModule;
import chipmunk.modules.runtime.CNull;

public class ClassVisitor implements AstVisitor {

	protected CClass cClass;
	protected List<Object> constantPool;

	protected MethodNode sharedInit;
	protected MethodNode instanceInit;
	
	protected CModule module;
	
	private boolean alreadyReachedConstructor;
	
	public ClassVisitor(CModule module){
		this(new ArrayList<>(), module);
	}
	
	public ClassVisitor(List<Object> constantPool, CModule module){
		this.constantPool = constantPool;
		this.module = module;
		alreadyReachedConstructor = false;
		
	}
	
	@Override
	public void visit(AstNode node) {
		
		if(node instanceof ClassNode){
			ClassNode classNode = (ClassNode) node;
			
			if(cClass == null) {
				cClass = new CClass(classNode.getName(), module);

				sharedInit = new MethodNode("<class init>");
				sharedInit.getSymbol().setShared(true);
				sharedInit.getSymbolTable().setParent(classNode.getSymbolTable());

				instanceInit = new MethodNode("<init>");
				instanceInit.getSymbolTable().setParent(classNode.getSymbolTable());

				classNode.addChild(sharedInit);
				classNode.addChild(instanceInit);

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
			
		}else if(node instanceof VarDecNode){
			// TODO - final variables
			VarDecNode varDec = (VarDecNode) node;
			
			VarDecVisitor visitor = null;
			final boolean isShared = varDec.getSymbol().isShared();
			final boolean isFinal = varDec.getSymbol().isFinal();
			final boolean isTrait = varDec.getSymbol().isTrait();

			if(varDec.getAssignExpr() != null){
				// Move the assignment to the relevant initializer
				AstNode expr = varDec.getAssignExpr();
				IdNode id = new IdNode(varDec.getIDNode().getID());

				OperatorNode assign = new OperatorNode(new Token("=", Token.Type.EQUALS));
				assign.getChildren().add(id);
				assign.getChildren().add(expr);

				varDec.setAssignExpr(null);

				if(isShared){
					sharedInit.addToBody(assign);
				}else{
					instanceInit.addToBody(assign);
				}
			}

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
				// non-constructors
				visitor = new MethodVisitor(constantPool, module);
				methodNode.visit(visitor);
			}
			
				
			CMethod method = visitor.getMethod();

			if(methodNode == sharedInit){
				// Shared initializer
				method.bind(cClass);
				cClass.setSharedInitializer(method);
			}else if(methodNode == instanceInit){
				// Instance initializer
				cClass.setInstanceInitializer(method);
			}else if(methodNode.getSymbol().isShared()){
				// Plain shared method
				method.bind(cClass);
				cClass.getAttributes().set(methodNode.getName(), method);
			}else{
				// Plain instance method
				cClass.getInstanceAttributes().set(methodNode.getName(), method);
			}
		}
		
		return;
	}
	
	public CClass getCClass(){
		
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
			constructor.setInstructions(assembler.getCodeSegment());
			constructor.getCode().setExceptionTable(new ExceptionBlock[]{});
			constructor.getCode().setDebugTable(new DebugEntry[]{});
			constructor.getCode().setDebugSymbol(cClass.getName() + "." + cClass.getName());
			
			cClass.getInstanceAttributes().set(cClass.getName(), constructor);
		}
		
		return cClass;
	}
	
	private void genInitCall(ChipmunkAssembler assembler){
		assembler.getLocal(0);
		assembler.init();
		assembler.call((byte)0);
		assembler.pop();
	}

}
