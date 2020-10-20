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

package chipmunk.compiler.codegen;

import java.util.ArrayList;
import java.util.List;

import chipmunk.binary.DebugEntry;
import chipmunk.binary.ExceptionBlock;
import chipmunk.binary.*;
import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.Token;
import chipmunk.compiler.ast.*;

public class ClassVisitor implements AstVisitor {

	protected BinaryClass cls;
	protected List<Object> constantPool;

	protected MethodNode sharedInit;
	protected MethodNode instanceInit;
	
	protected BinaryModule module;
	
	private boolean alreadyReachedConstructor;
	
	public ClassVisitor(BinaryModule module){
		this(new ArrayList<>(), module);
	}
	
	public ClassVisitor(List<Object> constantPool, BinaryModule module){
		this.constantPool = constantPool;
		this.module = module;
		alreadyReachedConstructor = false;
		
	}
	
	@Override
	public void visit(AstNode node) {
		
		if(node instanceof ClassNode){
			ClassNode classNode = (ClassNode) node;
			
			if(cls == null) {
				cls = new BinaryClass(classNode.getName(), module);

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
				BinaryClass inner = visitor.getBinaryClass();
				BinaryNamespace.Entry innerEntry = BinaryNamespace.Entry.makeClass(inner.getName(), (byte)0, inner);

				if(classNode.getSymbol().isShared()) {
					cls.getSharedFields().addEntry(innerEntry);
				}else {
					cls.getInstanceFields().addEntry(innerEntry);
				}
			}
			
		}else if(node instanceof VarDecNode){

			VarDecNode varDec = (VarDecNode) node;
			
			VarDecVisitor visitor = null;
			final boolean isShared = varDec.getSymbol().isShared();
			final boolean isFinal = varDec.getSymbol().isFinal();
			final boolean isTrait = varDec.getSymbol().isTrait();

			byte flags = 0;
			if(isFinal){
				flags |= BinaryConstants.FINAL_FLAG;
			}

			if(isTrait){
				flags |= BinaryConstants.TRAIT_FLAG;
			}

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

			BinaryNamespace clsNamespace;
			if(isShared){
				clsNamespace = cls.getSharedFields();
			}else{
				clsNamespace = cls.getInstanceFields();
			}

			clsNamespace.getEntries().add(new BinaryNamespace.Entry(varDec.getVarName(), flags));
			
		}else if(node instanceof MethodNode){
			MethodNode methodNode = (MethodNode) node;
			methodNode.addParam(0, new VarDecNode(new IdNode(new Token("self", Token.Type.IDENTIFIER))));
			
			MethodVisitor visitor = null;
			
			// this is the constructor
			if(methodNode.getSymbol().getName().equals(cls.getName())){
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
				
			BinaryMethod method = visitor.getMethod();
			BinaryNamespace.Entry methodEntry = BinaryNamespace.Entry.makeMethod(methodNode.getName(), (byte)0, method);

			if(methodNode == sharedInit){
				// Shared initializer
				cls.setSharedInitializer(method);
			}else if(methodNode == instanceInit){
				// Instance initializer
				cls.setInstanceInitializer(method);
			}else if(methodNode.getSymbol().isShared()){
				// Plain shared method
				cls.getSharedFields().addEntry(methodEntry);
			}else{
				// Plain instance method
				cls.getInstanceFields().addEntry(methodEntry);
			}
		}
		
		return;
	}
	
	public BinaryClass getBinaryClass(){
		
		// generate default constructor if no constructor was specified
		if(!alreadyReachedConstructor){
			ChipmunkAssembler assembler = new ChipmunkAssembler(constantPool);
			genInitCall(assembler);
			// return self
			assembler.getLocal(0);
			assembler._return();
			
			BinaryMethod constructor = new BinaryMethod();
			constructor.setArgCount(0);
			constructor.setLocalCount(1);
			constructor.setModule(module);
			constructor.setCode(assembler.getCodeSegment());
			constructor.setExceptionTable(new ExceptionBlock[]{});
			constructor.setDebugTable(new DebugEntry[]{});
			constructor.setDeclarationSymbol(cls.getName() + "." + cls.getName());
			
			cls.getInstanceFields().getEntries().add(BinaryNamespace.Entry.makeMethod(cls.getName(), (byte)0, constructor));
		}
		
		return cls;
	}
	
	private void genInitCall(ChipmunkAssembler assembler){
		//assembler.getLocal(0);
		//assembler.init();
		//assembler.call((byte)1);
		//assembler.pop();
	}

}
