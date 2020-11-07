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

import chipmunk.binary.*;
import chipmunk.compiler.assembler.ChipmunkAssembler;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.ast.*;

public class ClassVisitor implements AstVisitor {

	protected BinaryClass cls;
	protected List<Object> constantPool;
	
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
				classNode.visitChildren(this);
			}else {
				// visit nested class declarations
				ClassVisitor visitor = new ClassVisitor(constantPool, module);
				classNode.visit(visitor);
				BinaryClass inner = visitor.getBinaryClass();
				BinaryNamespace.Entry innerEntry = BinaryNamespace.Entry.makeClass(inner.getName(), (byte)0, inner);

				if(classNode.getSymbol().isShared()) {
					cls.getSharedNamespace().addEntry(innerEntry);
				}else {
					cls.getInstanceNamespace().addEntry(innerEntry);
				}
			}
			
		}else if(node instanceof VarDecNode){

			VarDecNode varDec = (VarDecNode) node;

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

			BinaryNamespace clsNamespace;
			if(isShared){
				clsNamespace = cls.getSharedNamespace();
			}else{
				clsNamespace = cls.getInstanceNamespace();
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

			if(methodNode.getSymbol().isShared()){
				// Shared method
				System.out.println("Adding shared method " + methodNode.getSymbol());
				cls.getSharedNamespace().addEntry(methodEntry);
			}else{
				// Instance method
				cls.getInstanceNamespace().addEntry(methodEntry);
			}
		}
		
		return;
	}
	
	public BinaryClass getBinaryClass(){
		
		// generate default constructor if no constructor was specified
		// TODO - move this to an AST transform
		/*if(!alreadyReachedConstructor){
			ChipmunkAssembler assembler = new ChipmunkAssembler(constantPool);
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
			
			cls.getInstanceNamespace().getEntries().add(BinaryNamespace.Entry.makeMethod(cls.getName(), (byte)0, constructor));
		}*/
		
		return cls;
	}

}
