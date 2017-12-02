package chipmunk.compiler.codegen;

import java.util.HashMap;
import java.util.Map;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.SymbolTable;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;

public class Codegen implements AstVisitor {

	protected Map<Class<? extends AstNode>, AstVisitor> visitors;
	protected ChipmunkAssembler assembler;
	protected SymbolTable symbols;
	
	public Codegen(ChipmunkAssembler assembler, SymbolTable symbols){
		visitors = new HashMap<Class<? extends AstNode>, AstVisitor>();
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
	
	public SymbolTable getSymbols(){
		return symbols;
	}
}
