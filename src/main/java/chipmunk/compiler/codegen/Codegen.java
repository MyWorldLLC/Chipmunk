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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chipmunk.ExceptionBlock;
import chipmunk.binary.BinaryModule;
import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.Symbol;
import chipmunk.compiler.SymbolTable;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.MethodNode;
import chipmunk.modules.runtime.CModule;

public class Codegen implements AstVisitor {

	protected Map<Class<? extends AstNode>, AstVisitor> visitors;
	protected ChipmunkAssembler assembler;
	protected SymbolTable symbols;
	
	protected BinaryModule module;
	
	protected List<LoopLabels> loopStack;
	protected List<IfElseLabels> ifElseStack;
	protected List<TryCatchLabels> tryCatchStack;
	
	protected List<ExceptionBlock> exceptions;
	
	
	public Codegen(BinaryModule module){
		visitors = new HashMap<>();
		loopStack = new ArrayList<>();
		ifElseStack = new ArrayList<>();
		tryCatchStack = new ArrayList<>();
		exceptions = new ArrayList<>();
		assembler = new ChipmunkAssembler();
		symbols = new SymbolTable();
		this.module = module;
	}
	
	public Codegen(ChipmunkAssembler assembler, SymbolTable symbols, BinaryModule module){
		visitors = new HashMap<>();
		loopStack = new ArrayList<>();
		ifElseStack = new ArrayList<>();
		tryCatchStack = new ArrayList<>();
		exceptions = new ArrayList<>();
		this.assembler = assembler;
		this.symbols = symbols;
		this.module = module;
	}
	
	public BinaryModule getModule() {
		return module;
	}
	
	public void setVisitorForNode(Class<? extends AstNode> nodeType, AstVisitor visitor){
		visitors.put(nodeType, visitor);
	}
	
	public void visit(AstNode node){
		AstVisitor visitor = visitors.get(node.getClass());
		
		if(visitor == null){
			throw new IllegalArgumentException("Unknown node type: " + node.getClass().getSimpleName());
		}
		
		node.visit(visitor);
	}
	
	public ChipmunkAssembler getAssembler(){
		return assembler;
	}
	
	public void enterScope(SymbolTable symbols){
		this.symbols = symbols;
	}
	
	public void exitScope(){
		if(symbols != null){
			symbols = symbols.getParent();
		}
	}
	
	public void emitSymbolAccess(String name){
		emitSymbolReference(name, false);
	}
	
	public void emitSymbolAssignment(String name){
		emitSymbolReference(name, true);
	}
	
	private void emitSymbolReference(String name, boolean assign){
		
		Deque<SymbolTable> trace = getSymbolTrace(name);
		
		if(trace == null){
			// no variable with that name was returned. Assume it was a module-level symbol
			if(assign){
				assembler.setModule(name);
			}else{
				assembler.getModule(name);
			}
			return;
		}
		
		// NOTE: in general use, the null check is unneeded because all accessing code
		// will be inside a method. In some of the tests, however, this is not the case
		// so leave this in here.
		SymbolTable methodTable = getMethodScope(trace);
		MethodNode method = null;
		if(methodTable != null){
			method = (MethodNode) methodTable.getNode();
		}
		
		SymbolTable table = trace.getLast();
		SymbolTable.Scope scope = table.getScope();
		
		if(scope == SymbolTable.Scope.LOCAL || scope == SymbolTable.Scope.METHOD){
			// local scope
			// TODO - closure support
			if(assign){
				assembler.setLocal(table.getLocalIndex(name));
			}else{
				assembler.getLocal(table.getLocalIndex(name));
			}
		}else if(scope == SymbolTable.Scope.CLASS){
			Symbol symbol = table.getSymbol(name);
			
			if(symbol.isShared()){
				// initializers aren't enclosed by a method
				// shared and non-shared variable initializers both bind via "self"
				// object
				if(method == null || method.getSymbol().isShared()){
					// shared method reference to shared variable. Self
					// refers to class, so emit reference via self
					if(assign){
						assembler.push(symbol.getName());
						assembler.getLocal(0);
						assembler.setattr();
					}else{
						assembler.push(symbol.getName());
						assembler.getLocal(0);
						assembler.getattr();
					}
					
				}else{
					// TODO - instance method reference to shared variable. Get class and reference variable
					// as shared attribute
					assembler.push(symbol.getName());
					assembler.getLocal(0);
					assembler.callAt("getClass", (byte)0);
					if(assign){
						assembler.setattr();
					}else{
						assembler.getattr();
					}
				}
			}else{
				if(method != null && method.getSymbol().isShared()){
					// TODO - shared method reference to instance variable. Illegal.
				}else{
					// TODO - instance method (or initializer) reference to instance variable. Emit
					// reference via self
					
					if(assign){
						assembler.push(symbol.getName());
						assembler.getLocal(0);
						assembler.setattr();
					}else{
						assembler.push(symbol.getName());
						assembler.getLocal(0);
						assembler.getattr();
					}
				}
			}
		}else if(scope == SymbolTable.Scope.MODULE){
			// Module
			if(assign){
				assembler.setModule(name);
			}else{
				assembler.getModule(name);
			}
		}
	}
	
	private Deque<SymbolTable> getSymbolTrace(String name){
		Deque<SymbolTable> trace = new ArrayDeque<SymbolTable>();
		
		SymbolTable symTab = symbols;
		
		boolean found = false;
		while(!found){
			trace.add(symTab);
			
			for(Symbol symbol : symTab.getAllSymbols()){
				if(symbol.getName().equals(name)){
					found = true;
					break;
				}
			}
			
			if(!found){
				symTab = symTab.getParent();
				if(symTab == null){
					// variable was not found
					return null;
				}
			}
		}
		return trace;
	}
	
	private SymbolTable getMethodScope(Deque<SymbolTable> trace){
		for(SymbolTable table : trace){
			if(table.getScope() == SymbolTable.Scope.METHOD){
				return table;
			}
		}
		return null;
	}
	
	public SymbolTable getActiveSymbols(){
		return symbols;
	}
	
	public LoopLabels pushLoop(){
		LoopLabels labels = new LoopLabels(assembler.nextLabelName(), assembler.nextLabelName(), assembler.nextLabelName());
		loopStack.add(labels);
		return labels;
	}
	
	public LoopLabels peekClosestLoop(){
		if(loopStack.size() > 0){
			return loopStack.get(loopStack.size() - 1);
		}
		return null;
	}
	
	public LoopLabels exitLoop(){
		if(loopStack.size() > 0){
			LoopLabels labels = loopStack.get(loopStack.size() - 1);
			loopStack.remove(loopStack.size() - 1);
			return labels;
		}
		return null;
	}
	
	public boolean inLoop(){
		return loopStack.size() > 0;
	}
	
	public IfElseLabels pushIfElse() {
		IfElseLabels labels = new IfElseLabels(assembler.nextLabelName());
		ifElseStack.add(labels);
		return labels;
	}
	
	public IfElseLabels peekClosestIfElse() {
		if(ifElseStack.size() > 0) {
			return ifElseStack.get(ifElseStack.size() - 1);
		}
		return null;
	}
	
	public IfElseLabels exitIfElse(){
		if(ifElseStack.size() > 0){
			IfElseLabels labels = ifElseStack.get(ifElseStack.size() - 1);
			ifElseStack.remove(ifElseStack.size() - 1);
			return labels;
		}
		return null;
	}
	
	public boolean inIfElse(){
		return ifElseStack.size() > 0;
	}
	
	public TryCatchLabels pushTryCatch() {
		TryCatchLabels labels = new TryCatchLabels(assembler.nextLabelName(), assembler.nextLabelName());
		tryCatchStack.add(labels);
		return labels;
	}
	
	public TryCatchLabels peekClosestTryCatch() {
		if(tryCatchStack.size() > 0) {
			return tryCatchStack.get(tryCatchStack.size() - 1);
		}
		return null;
	}
	
	public TryCatchLabels exitTryCatch(){
		if(tryCatchStack.size() > 0){
			TryCatchLabels labels = tryCatchStack.get(tryCatchStack.size() - 1);
			tryCatchStack.remove(tryCatchStack.size() - 1);
			return labels;
		}
		return null;
	}
	
	public boolean inTryCatch(){
		return tryCatchStack.size() > 0;
	}
	
	public void addExceptionBlock(ExceptionBlock block) {
		exceptions.add(block);
	}
	
	public List<ExceptionBlock> getExceptionBlocks(){
		return exceptions;
	}
}
