package chipmunk.compiler.ast;

import chipmunk.compiler.Symbol;
import chipmunk.compiler.SymbolTable;

public class MethodNode extends BlockNode implements SymbolNode {
	
	protected int defaultParamCount;
	protected int paramCount;
	protected Symbol symbol;
	
	public MethodNode(){
		super(SymbolTable.Scope.METHOD);
		symbol = new Symbol();
	}
	
	public MethodNode(String name){
		this();
		symbol.setName(name);
	}
	
	public String getName(){
		return symbol.getName();
	}
	
	public void setName(String name){
		symbol.setName(name);
	}
	
	public int getParamCount(){
		return paramCount;
	}

	public void setParamCount(int paramCount){
		this.paramCount = paramCount;
	}

	public void setDefaultParamCount(int count){
		defaultParamCount = count;
	}
	
	public int getDefaultParamCount(){
		return defaultParamCount;
	}
	
	public void addToBody(AstNode node){
		addChild(node);
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("(method ");
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
