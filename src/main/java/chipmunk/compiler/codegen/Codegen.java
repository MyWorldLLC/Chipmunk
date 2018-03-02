package chipmunk.compiler.codegen;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.Symbol;
import chipmunk.compiler.SymbolTable;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.MethodNode;

public class Codegen implements AstVisitor {

	protected Map<Class<? extends AstNode>, AstVisitor> visitors;
	protected ChipmunkAssembler assembler;
	protected SymbolTable symbols;
	
	protected List<LoopLabels> loopStack;
	
	public Codegen(){
		visitors = new HashMap<Class<? extends AstNode>, AstVisitor>();
		loopStack = new ArrayList<LoopLabels>();
		assembler = new ChipmunkAssembler();
		symbols = new SymbolTable();
	}
	
	public Codegen(ChipmunkAssembler assembler, SymbolTable symbols){
		visitors = new HashMap<Class<? extends AstNode>, AstVisitor>();
		loopStack = new ArrayList<LoopLabels>();
		this.assembler = assembler;
		this.symbols = symbols;
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
				if(method.getSymbol().isShared()){
					// shared method reference to shared variable. Self
					// refers to class, so emit reference via self
				}else{
					// TODO - instance method reference to shared variable.
				}
			}else{
				if(method.getSymbol().isShared()){
					// TODO - shared method reference to instance variable. Illegal.
				}else{
					// TODO - instance method reference to instance variable. Emit
					// reference via self
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
					// TODO - need better exception type
					// TODO - undeclared variables as module variables?
					throw new IllegalArgumentException("Undeclared variable " + name);
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
	
}
