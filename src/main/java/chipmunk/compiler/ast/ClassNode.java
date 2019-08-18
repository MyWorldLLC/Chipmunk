package chipmunk.compiler.ast;

import chipmunk.compiler.Symbol;
import chipmunk.compiler.SymbolTable;

public class ClassNode extends BlockNode implements SymbolNode {
	
	protected Symbol symbol;
	
	public ClassNode(){
		super(SymbolTable.Scope.CLASS);
		symbol = new Symbol();
	}
	
	public ClassNode(String name){
		this();
		setName(name);
	}

	public String getName(){
		return symbol.getName();
	}

	public void setName(String name){
		symbol.setName(name);
		getSymbolTable().setDebugSymbol(name);
	}

	public void addChild(AstNode child){
		super.addChild(child);
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("(class ");
		builder.append(symbol.getName());
		
		for(AstNode child : children){
			builder.append(' ');
			builder.append(child.toString());
		}
		
		builder.append(")");
		return builder.toString();
	}

	@Override
	public Symbol getSymbol() {
		return symbol;
	}
}
