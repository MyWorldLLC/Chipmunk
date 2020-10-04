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

import chipmunk.DebugEntry;
import chipmunk.ExceptionBlock;
import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.Token;
import chipmunk.compiler.ast.*;
import chipmunk.modules.runtime.CClass;
import chipmunk.modules.runtime.CMethod;
import chipmunk.modules.runtime.CModule;
import chipmunk.modules.runtime.CNull;

public class ModuleVisitor implements AstVisitor {
	
	protected CModule module;
	protected Codegen initCodegen;
	protected ChipmunkAssembler initAssembler;

	protected MethodNode initMethod;

	protected List<Object> constantPool;
	
	public ModuleVisitor(){
		constantPool = new ArrayList<>();
		initAssembler = new ChipmunkAssembler(constantPool);
	}

	@Override
	public void visit(AstNode node) {
		if(node instanceof ModuleNode){
			
			ModuleNode moduleNode = (ModuleNode) node;
			moduleNode.getSymbolTable().setDebugSymbol(moduleNode.getName());
			module = new CModule(moduleNode.getSymbol().getName(), constantPool);
			initCodegen = new Codegen(initAssembler, moduleNode.getSymbolTable(), module);
			initMethod = new MethodNode("<module init>");
			module.getImports().add(new CModule.Import("chipmunk.lang", true));
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
			
			CModule.Import im = new CModule.Import(importNode.getModule(), importAll);
			
			if(!importAll){
				im.getSymbols().addAll(importNode.getSymbols());
				im.getAliases().addAll(importNode.getAliases());
			}
			
			module.getImports().add(im);
		}else if(node instanceof VarDecNode){
			
			// TODO - final variables
			VarDecNode varDec = (VarDecNode) node;
			
			VarDecVisitor visitor = new VarDecVisitor(initCodegen);

			if(varDec.getAssignExpr() != null) {
				// Move the assignment to the module initializer
				AstNode expr = varDec.getAssignExpr();
				IdNode id = new IdNode(varDec.getIDNode().getID());

				OperatorNode assign = new OperatorNode(new Token("=", Token.Type.EQUALS));
				assign.getChildren().add(id);
				assign.getChildren().add(expr);

				varDec.setAssignExpr(null);

				initMethod.addToBody(assign);
			}

			visitor.visit(varDec);
			
			module.getNamespace().set(varDec.getVarName(), CNull.instance());
		}else{
			throw new IllegalArgumentException("Error parsing module " + module.getName() + ": illegal AST node type " + node.getClass());
		}
	}
	
	public CModule getModule(){

		MethodVisitor initVisitor = new MethodVisitor(initAssembler, module);

		Set<String> importedModules = new HashSet<>();
		for(int i = 0; i < module.getImports().size(); i++){
			CModule.Import im = module.getImports().get(i);

			if(!importedModules.contains(im.getName())){
				importedModules.add(im.getName());
				initAssembler.initModule(i);
			}

			initAssembler._import(i);
		}

		initVisitor.visit(initMethod);

		CMethod initializer = initVisitor.getMethod();

		/*initializer.getCode().setConstantPool(initAssembler.getConstantPool().toArray());
		initializer.getCode().setCode(initAssembler.getCodeSegment());
		initializer.getCode().setLocalCount(0);
		initializer.getCode().setExceptionTable(initCodegen.getExceptionBlocks().toArray(new ExceptionBlock[]{}));
		initializer.getCode().setDebugTable(initCodegen.getAssembler().getDebugTable().toArray(new DebugEntry[]{}));
		initializer.getCode().setDebugSymbol(module.getName() + ".<module init>");*/
		
		initializer.bind(module);
		initializer.getCode().setModule(module);
		module.setInitializer(initializer);
		
		return module;
	}

}
