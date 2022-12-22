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

import java.util.List;

import chipmunk.binary.DebugEntry;
import chipmunk.binary.ExceptionBlock;
import chipmunk.binary.BinaryMethod;
import chipmunk.binary.BinaryModule;
import chipmunk.compiler.assembler.ChipmunkAssembler;
import chipmunk.compiler.ast.*;
import chipmunk.compiler.symbols.Symbol;
import chipmunk.compiler.symbols.SymbolTable;
import static chipmunk.compiler.ast.NodeType.*;

public class MethodVisitor implements AstVisitor {

	protected BinaryMethod method;
	protected ChipmunkAssembler assembler;
	protected Codegen outerCodegen;
	protected SymbolTable symbols;
	protected Codegen codegen;
	protected AstNode methodNode;
	
	protected BinaryModule module;
	
	protected boolean defaultReturn;
	
	protected boolean isInner;
	
	
	public MethodVisitor(ChipmunkAssembler assembler, BinaryModule module){
		this.assembler = assembler;
		defaultReturn = true;
		isInner = false;
		this.module = module;
	}
	
	public MethodVisitor(List<Object> constantPool, BinaryModule module){
		assembler = new ChipmunkAssembler(constantPool);
		defaultReturn = true;
		isInner = false;
		this.module = module;
	}
	
	public MethodVisitor(Codegen outerCodegen, List<Object> constantPool, BinaryModule module){
		assembler = new ChipmunkAssembler(constantPool);
		this.outerCodegen = outerCodegen;
		defaultReturn = true;
		isInner = true;
		this.module = module;
	}
	
	@Override
	public void visit(AstNode node) {
		
		method = new BinaryMethod();
		
		if(node.is(METHOD)){
			methodNode = node;

			method.setArgCount(Methods.getParamCount(node));
			//method.setDefaultArgCount(methodNode.getDefaultParamCount());
			
			symbols = methodNode.getSymbolTable();
			method.setDeclarationSymbol(symbols.getDebugSymbol());
			
			codegen = new Codegen(assembler, symbols, module);
			
			ExpressionStatementVisitor expStatVisitor = new ExpressionStatementVisitor(codegen);

			codegen.setVisitorForNode(OPERATOR, expStatVisitor);
			codegen.setVisitorForNode(ID, new NoOpVisitor()); // Handle id nodes that are on their own lines
			codegen.setVisitorForNode(METHOD, new MethodVisitor(codegen, assembler.getConstantPool(), module));
			//codegen.setVisitorForNode(ClassNode.class, new ClassVisitor(assembler.getConstantPool(), module, assembler));
			codegen.setVisitorForNode(VAR_DEC, new VarDecVisitor(codegen));
			codegen.setVisitorForNode(IF_ELSE, new IfElseVisitor(codegen));
			codegen.setVisitorForNode(WHILE, new WhileVisitor(codegen));
			codegen.setVisitorForNode(FOR, new ForVisitor(codegen));
			codegen.setVisitorForNode(FLOW_CONTROL, new FlowControlVisitor(codegen));
			codegen.setVisitorForNode(TRY_CATCH, new TryCatchVisitor(codegen));
			
			// The VM sets the locals for arguments for us - we only need to handle default arguments
			// that aren't supplied in the call.
			// TODO - this will overwrite all default arguments that were supplied. To support these
			// properly, generate code to determine number of arguments in call and jump to the right
			// point to initialize non-supplied arguments.
			/*int startAt = Methods.getParamCount(methodNode);
			if(methodNode.hasDefaultParams()){
				startAt -= methodNode.getDefaultParamCount();
				// TODO
			}*/
			
			codegen.enterScope(symbols);
			if(Methods.getBodyNodeCount(methodNode) == 1
					&& ExpressionVisitor.isExpressionNode(methodNode.getChild(methodNode.childCount() - 1))) {
				// this supports "lambda" methods - single expression methods that automatically return without the "return" keyword
				ExpressionVisitor visitor = new ExpressionVisitor(codegen);
				visitor.visit(methodNode.getChild(methodNode.childCount() - 1));
				assembler._return();
			}else {
				// regular methods
				methodNode.visitChildren(codegen, 2);
			}
			codegen.exitScope();
			
			if(defaultReturn){
				// return null in case a return has not yet been hit
				genDefaultReturn();
			}
			
			// non-lambda methods are declared using statement block syntax. To support this, the result of assembling an
			// inner method must be saved as a local variable in the containing method.
			// TODO - forbid empty inner method names for non-lambda methods.
			/*if(isInner) {
				outerCodegen.getAssembler().push(getMethod());
				outerCodegen.emitSymbolAssignment(getMethodSymbol().getName());
			}*/
		}
		
	}
	
	public void genSelfReturn(){
		assembler.getLocal(0);
		assembler._return();
	}
	
	public void genDefaultReturn(){
		assembler.pushNull();
		assembler._return();
	}
	
	public boolean willGenDefaultReturn(){
		return defaultReturn;
	}
	
	public void setDefaultReturn(boolean defaultReturn){
		this.defaultReturn = defaultReturn;
	}
	
	public BinaryMethod getMethod(){

		method.setCode(assembler.getCodeSegment());
		method.setLocalCount(symbols.getLocalMax());
		method.setModule(module);
		method.setExceptionTable(codegen.getExceptionBlocks().toArray(new ExceptionBlock[]{}));
		method.setDebugTable(codegen.getAssembler().getDebugTable().toArray(new DebugEntry[]{}));
		
		return method;
	}
	
	public Symbol getMethodSymbol(){
		return methodNode.getSymbol();
	}

}
