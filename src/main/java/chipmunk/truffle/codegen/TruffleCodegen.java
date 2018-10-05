package chipmunk.truffle.codegen;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import com.oracle.truffle.api.nodes.Node;

import chipmunk.compiler.Symbol;
import chipmunk.compiler.SymbolTable;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.MethodNode;

public class TruffleCodegen {

	protected Map<Class<? extends AstNode>, TruffleAstVisitor<?>> visitors;
	protected SymbolTable symbols;
	
	public TruffleCodegen(){
		visitors = new HashMap<Class<? extends AstNode>, TruffleAstVisitor<?>>();
		symbols = new SymbolTable();
		
		// TODO - register visitors
	}
	
	public Node emit(AstNode node) {
		TruffleAstVisitor<?> visitor = visitors.get(node.getClass());
		
		if(visitor == null){
			throw new IllegalArgumentException("Unknown node type: " + node.getClass().getSimpleName());
		}
		
		return visitor.visit(node);
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
				//assembler.setModule(name);
			}else{
				//assembler.getModule(name);
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
				//assembler.setLocal(table.getLocalIndex(name));
			}else{
				//assembler.getLocal(table.getLocalIndex(name));
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
						///assembler.push(symbol.getName());
						//assembler.getLocal(0);
						//assembler.setattr();
					}else{
						//assembler.push(symbol.getName());
						//assembler.getLocal(0);
						///assembler.getattr();
					}
					
				}else{
					// TODO - instance method reference to shared variable. Get class and reference variable
					// as shared attribute
					///assembler.push(symbol.getName());
					//assembler.getLocal(0);
					//assembler.callAt("getClass", (byte)0);
					if(assign){
						//assembler.setattr();
					}else{
						//assembler.getattr();
					}
				}
			}else{
				if(method != null && method.getSymbol().isShared()){
					// TODO - shared method reference to instance variable. Illegal.
				}else{
					// TODO - instance method (or initializer) reference to instance variable. Emit
					// reference via self
					
					if(assign){
						//assembler.push(symbol.getName());
						//assembler.getLocal(0);
						//assembler.setattr();
					}else{
						//assembler.push(symbol.getName());
						//assembler.getLocal(0);
						//assembler.getattr();
					}
				}
			}
		}else if(scope == SymbolTable.Scope.MODULE){
			// Module
			if(assign){
				//assembler.setModule(name);
			}else{
				//assembler.getModule(name);
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
	
}
