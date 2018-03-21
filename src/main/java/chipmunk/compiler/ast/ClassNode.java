package chipmunk.compiler.ast;

import chipmunk.compiler.Symbol;
import chipmunk.compiler.SymbolTable;

public class ClassNode extends BlockNode implements SymbolNode {
	
	protected boolean isFinal;
	protected String superName;
	protected Symbol symbol;
	
	public ClassNode(){
		super(SymbolTable.Scope.CLASS);
		symbol = new Symbol();
		isFinal = false;
	}
	
	public ClassNode(String name){
		super();
		symbol.setName(name);
	}
	
	public ClassNode(String name, String superName){
		super();
		symbol.setName(name);
		this.superName = superName;
	}

	public String getName(){
		return symbol.getName();
	}

	public void setName(String name){
		symbol.setName(name);
	}
	
	public boolean isFinal(){
		return isFinal;
	}
	
	public void setFinal(boolean isFinal){
		this.isFinal = isFinal;
	}

	public String getSuperName(){
		return superName;
	}

	public void setSuperName(String superName){
		this.superName = superName;
	}

	public void addChild(AstNode child){
		super.addChild(child);
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("(class ");
		builder.append(symbol.getName());
		
		if(superName != null){
			builder.append(" extends ");
			builder.append(superName);
		}
		
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
