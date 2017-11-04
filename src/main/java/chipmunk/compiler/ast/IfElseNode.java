package chipmunk.compiler.ast;

public class IfElseNode extends GuardedNode {
	
	protected boolean hasElse;
	
	public IfElseNode(){
		hasElse = false;
	}
	
	public AstNode getElseBranch(){
		if(hasElse){
			return children.get(children.size() - 1);
		}else{
			return null;
		}
	}
	
	public void setElseBranch(AstNode branch){
		if(branch != null){
			hasElse = true;
			super.addChild(branch);
		}else{
			if(hasElse){
				children.remove(children.size() - 1);
			}
			hasElse = false;
		}
	}
	
	public void addChild(AstNode child){
		if(!hasElse){
			super.addChild(child);
		}else{
			children.add(children.size() - 2, child);
		}
		
	}
	
	public void addChildren(AstNode... children){
		if(!hasElse){
			super.addChildren(children);
		}else{
			for(AstNode child : children){
				this.children.add(this.children.size() - 2, child);
			}
		}
	}

}
