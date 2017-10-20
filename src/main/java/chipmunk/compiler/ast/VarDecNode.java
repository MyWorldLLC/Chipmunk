package chipmunk.compiler.ast;

public class VarDecNode extends AstNode {
	
	protected boolean hasVar;
	protected boolean hasAssignExpr;
	
	public VarDecNode(){
		super();
		hasVar = false;
		hasAssignExpr = false;
	}
	
	public void setVar(IdNode id){
		if(hasVar){
			children.remove(0);
		}
		
		if(id != null){
			this.addChildFirst(id);
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

}
