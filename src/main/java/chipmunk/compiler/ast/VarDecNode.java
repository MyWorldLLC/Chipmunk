package chipmunk.compiler.ast;

import chipmunk.compiler.Symbol;

public class VarDecNode extends AstNode implements SymbolNode {
	
	protected boolean hasVar;
	protected boolean hasAssignExpr;
	protected Symbol symbol;
	
	
	public VarDecNode(){
		super();
		hasVar = false;
		hasAssignExpr = false;
		symbol = new Symbol();
	}
	
	public VarDecNode(IdNode id){
		this();
		hasVar = true;
		super.addChild(id);
		symbol.setName(id.getID().getText());
	}
	
	public void setVar(IdNode id){
		if(hasVar){
			children.remove(0);
		}
		
		if(id != null){
			this.addChildFirst(id);
			symbol.setName(id.getID().getText());
			hasVar = true;
		}else{
			hasVar = false;
		}
	}
	
	public void setAssignExpr(AstNode expr){
		if(hasAssignExpr){
			children.remove(children.size() - 1);
		}
		
		if(expr != null){
			this.addChild(expr);
			hasAssignExpr = true;
		}else{
			hasAssignExpr = false;
		}
	}
	
	public IdNode getIDNode(){
		return hasVar ? (IdNode) children.get(0) : null;
	}
	
	public AstNode getAssignExpr(){
		return hasAssignExpr ? children.get(1) : null;
	}
	
	public String getVarName(){
		return hasVar ? getIDNode().getID().getText() : null;
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		
		builder.append("(vardec ");
		
		if(hasVar){
			builder.append(getIDNode().getID().getText());
			
			if(hasAssignExpr){
				builder.append(' ');
			}
		}
		
		if(hasAssignExpr){
			builder.append(getAssignExpr().toString());
		}
		builder.append(")");
		return builder.toString();
	}

	@Override
	public Symbol getSymbol() {
		return symbol;
	}

}
