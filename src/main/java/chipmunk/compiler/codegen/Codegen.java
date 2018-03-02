package chipmunk.compiler.codegen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.Symbol;
import chipmunk.compiler.SymbolTable;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;

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
		SymbolTable symTab = symbols;
		
		boolean found = false;
		while(!found){
			for(Symbol symbol : symTab.getAllSymbols()){
				if(symbol.getName().equals(name)){
					// found symbol - emit access
					if(symTab.getScope() == SymbolTable.Scope.LOCAL || symTab.getScope() == SymbolTable.Scope.METHOD){
						// local scope
						if(assign){
							assembler.setLocal(symTab.getLocalIndex(name));
						}else{
							assembler.getLocal(symTab.getLocalIndex(name));
						}
					}else if(symTab.getScope() == SymbolTable.Scope.CLASS){
						// TODO - class instance & shared
					}else{
						// Module
						if(assign){
							assembler.setModule(name);
						}else{
							assembler.getModule(name);
						}
					}
					found = true;
					break;
				}
			}
			
			if(!found){
				// TODO - support "tracing" through multiple nested method & class scopes
				symTab = symTab.getParent();
				if(symTab == null){
					// TODO - throw new UnresolvedSymbolChipmunk(String.format("Undeclared variable %s at %s: %d",
					//		symbolToken.getText(), symbolToken.getFile(), symbolToken.getLine()), symbolToken);
					throw new IllegalArgumentException("Undeclared variable " + name);
				}
			}
			
		}
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
