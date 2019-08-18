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
		defaultParamCount = 0;
		paramCount = 0;
	}
	
	public MethodNode(String name){
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
	
	public int getParamCount(){
		return paramCount;
	}
	
	public boolean hasParams(){
		return paramCount != 0;
	}
	
	public int getDefaultParamCount(){
		return defaultParamCount;
	}
	
	public boolean hasDefaultParams(){
		return defaultParamCount != 0;
	}
	
	public void addParam(VarDecNode param){
		children.add(paramCount, param);
		paramCount++;
		
		if(param.getAssignExpr() != null){
			defaultParamCount++;
		}
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
