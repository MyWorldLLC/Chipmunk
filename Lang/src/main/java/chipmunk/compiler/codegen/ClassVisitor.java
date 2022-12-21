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
		
		if(node.is(NodeType.CLASS)){
			
			if(cls == null) {
				cls = new BinaryClass(node.getSymbol().getName(), module);
				node.visitChildren(this);
			}else {
				// visit nested class declarations
				ClassVisitor visitor = new ClassVisitor(constantPool, module);
				node.visit(visitor);
				BinaryClass inner = visitor.getBinaryClass();
				BinaryNamespace.Entry innerEntry = BinaryNamespace.Entry.makeClass(inner.getName(), (byte)0, inner);

				if(node.getSymbol().isShared()) {
					cls.getSharedNamespace().addEntry(innerEntry);
				}else {
					cls.getInstanceNamespace().addEntry(innerEntry);
				}
			}
			
		}else if(node.is(NodeType.VAR_DEC)){

			final boolean isShared = node.getSymbol().isShared();
			final boolean isFinal = node.getSymbol().isFinal();
			final boolean isTrait = node.getSymbol().isTrait();

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

			clsNamespace.getEntries().add(new BinaryNamespace.Entry(VarDec.getIdentifier(node).getToken().text(), flags));
			
		}else if(node instanceof MethodNode){
			MethodNode methodNode = (MethodNode) node;
			
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
				cls.getSharedNamespace().addEntry(methodEntry);
			}else{
				// Instance method
				cls.getInstanceNamespace().addEntry(methodEntry);
			}
		}
		
		return;
	}
	
	public BinaryClass getBinaryClass(){
		return cls;
	}

}
