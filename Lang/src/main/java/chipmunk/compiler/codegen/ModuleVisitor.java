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
import chipmunk.compiler.ast.*;

public class ModuleVisitor implements AstVisitor {
	
	protected BinaryModule module;
	protected Codegen initCodegen;

	protected List<Object> constantPool;
	protected List<BinaryImport> imports;
	protected BinaryNamespace namespace;
	
	public ModuleVisitor(){
		constantPool = new ArrayList<>();
		imports = new ArrayList<>();
		namespace = new BinaryNamespace();
	}

	@Override
	public void visit(AstNode node) {
		if(node instanceof ModuleNode){
			
			ModuleNode moduleNode = (ModuleNode) node;
			moduleNode.getSymbolTable().setDebugSymbol(moduleNode.getName());

			module = new BinaryModule(moduleNode.getSymbol().getName());

			//imports.add(new BinaryImport("chipmunk.lang", true));

			moduleNode.visitChildren(this);
			
		}else if(node instanceof ClassNode){
			
			ClassVisitor visitor = new ClassVisitor(constantPool, module);
			node.visit(visitor);
			
			BinaryClass cls = visitor.getBinaryClass();

			module.getNamespace().addEntry(BinaryNamespace.Entry.makeClass(cls.getName(), (byte)0, cls));
			
		}else if(node instanceof MethodNode){
			
			MethodVisitor visitor = new MethodVisitor(constantPool, module);
			node.visit(visitor);
			BinaryMethod method = visitor.getMethod();

			module.getNamespace().addEntry(
					BinaryNamespace.Entry.makeMethod(visitor.getMethodSymbol().getName(), (byte)0, method));
			
		}else if(node instanceof ImportNode){
			
			ImportNode importNode = (ImportNode) node;
			boolean importAll = importNode.isImportAll();
			
			BinaryImport im = new BinaryImport(importNode.getModule(), importAll);
			
			if(!importAll){
				im.setSymbols(importNode.getSymbols().toArray(new String[]{}));
				im.setAliases(importNode.getAliases().toArray(new String[]{}));
			}

			imports.add(im);

		}else if(node instanceof VarDecNode){

			VarDecNode varDec = (VarDecNode) node;

			byte flags = 0;
			if(varDec.getSymbol().isFinal()){
				flags |= BinaryConstants.FINAL_FLAG;
			}

			// At this point, if there was an in-line assignment for this it was already moved to an initializer
			// AST, so we don't need to do anything except add the method to the module namespace.
			
			module.getNamespace().getEntries().add(BinaryNamespace.Entry.makeField(varDec.getVarName(), flags));
		}else{
			throw new IllegalArgumentException("Error parsing module " + module.getName() + ": illegal AST node type " + node.getClass());
		}
	}
	
	public BinaryModule getModule(){
		module.setConstantPool(constantPool.toArray());
		module.setImports(imports.toArray(new BinaryImport[]{}));
		
		return module;
	}

}
