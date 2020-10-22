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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import chipmunk.binary.*;
import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.Token;
import chipmunk.compiler.ast.*;

public class ModuleVisitor implements AstVisitor {
	
	protected BinaryModule module;
	protected Codegen initCodegen;
	//protected ChipmunkAssembler initAssembler;

	//protected MethodNode initMethod;

	protected List<Object> constantPool;
	protected List<BinaryImport> imports;
	protected BinaryNamespace namespace;
	
	public ModuleVisitor(){
		constantPool = new ArrayList<>();
		imports = new ArrayList<>();
		namespace = new BinaryNamespace();

		//initAssembler = new ChipmunkAssembler(constantPool);
	}

	@Override
	public void visit(AstNode node) {
		if(node instanceof ModuleNode){
			
			ModuleNode moduleNode = (ModuleNode) node;
			moduleNode.getSymbolTable().setDebugSymbol(moduleNode.getName());

			module = new BinaryModule(moduleNode.getSymbol().getName());

			//initCodegen = new Codegen(initAssembler, moduleNode.getSymbolTable(), module);
			//initMethod = new MethodNode("<module init>");

			imports.add(new BinaryImport("chipmunk.lang", true));

			moduleNode.visitChildren(this);
			
		}else if(node instanceof ClassNode){
			
			ClassVisitor visitor = new ClassVisitor(constantPool, module);
			node.visit(visitor);
			
			BinaryClass cls = visitor.getBinaryClass();
			
			// generate initialization code to run class initializer
			//if(cls.getSharedInitializer() != null){
				//ChipmunkAssembler initAssembler = initCodegen.getAssembler();
				
				//initAssembler.getModule(cls.getName());
				//initAssembler.init();
				//initAssembler.call((byte)1);
				//initAssembler.pop();
			//}

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

			//VarDecVisitor visitor = new VarDecVisitor(initCodegen);

			/*if(varDec.getAssignExpr() != null) {
				// Move the assignment to the module initializer
				AstNode expr = varDec.getAssignExpr();
				IdNode id = new IdNode(varDec.getIDNode().getID());

				OperatorNode assign = new OperatorNode(new Token("=", Token.Type.EQUALS));
				assign.getChildren().add(id);
				assign.getChildren().add(expr);

				varDec.setAssignExpr(null);

				initMethod.addToBody(assign);
			}*/

			//visitor.visit(varDec);
			
			module.getNamespace().getEntries().add(BinaryNamespace.Entry.makeField(varDec.getVarName(), flags));
		}else{
			throw new IllegalArgumentException("Error parsing module " + module.getName() + ": illegal AST node type " + node.getClass());
		}
	}
	
	public BinaryModule getModule(){
		module.setConstantPool(constantPool.toArray());
		module.setImports(imports.toArray(new BinaryImport[]{}));

		//MethodVisitor initVisitor = new MethodVisitor(initAssembler, module);

		Set<String> importedModules = new HashSet<>();
		for(int i = 0; i < module.getImports().length; i++){
			BinaryImport im = module.getImports()[i];

			if(!importedModules.contains(im.getName())){
				importedModules.add(im.getName());
				//initAssembler.initModule(i);
			}

			//initAssembler._import(i);
		}

		//initVisitor.visit(initMethod);
		//module.setInitializer(initVisitor.getMethod());
		
		return module;
	}

}
