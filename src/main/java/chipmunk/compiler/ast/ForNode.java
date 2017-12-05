package chipmunk.compiler.ast;

public class ForNode extends BlockNode {
	
	protected boolean hasIter;
	protected boolean hasID;
	
	public ForNode(){
		hasIter = false;
		hasID = false;
	}
	
	public boolean hasIter(){
		return hasIter;
	}
	
	public boolean hasID(){
		return hasID;
	}
	
	public void setIter(AstNode iterExpr){
		if(iterExpr == null){
			if(hasIter && hasID){
				children.remove(1);
			}else if(hasIter && !hasID){
				children.remove(0);
			}
			hasIter = false;
		}else{
			if(hasIter && hasID){
				children.remove(1);
			}else if(hasIter && !hasID){
				children.remove(0);
			}
			if(!hasID){
				children.add(0, iterExpr);
			}else{
				children.add(1, iterExpr);
			}
			hasIter = true;
		}
	}
	
	public AstNode getIter(){
		if(hasIter && !hasID){
			return children.get(0);
		}else if(hasIter){
			return children.get(1);
			
		}else{
			return null;
		}
	}
	
	public void setID(VarDecNode id){
		if(id == null){
			if(hasID){
				children.remove(0);
			}
			hasID = false;
		}else{
			if(hasID){
				children.remove(0);
			}
			children.add(0, id);
			hasID = true;
		}
	}
	
	public VarDecNode getID(){
		if(hasID){
			return (VarDecNode) children.get(0);
		}else{
			return null;
		}
	}

}
