package chipmunk.compiler.ast;

public class IfElseNode extends GuardedNode {
	
	protected boolean hasElse;
	
	public IfElseNode(){
		hasElse = false;
	}
	
	public ScopedNode getElseBranch(){
		if(hasElse){
			return (ScopedNode) children.get(children.size() - 1);
		}else{
			return null;
		}
	}
	
	public void setElseBranch(ScopedNode branch){
		if(branch != null){
			if(hasElse){
				children.remove(children.size() - 1);
			}
			super.addChild(branch);
			hasElse = true;
		}else{
			if(hasElse){
				children.remove(children.size() - 1);
			}
			hasElse = false;
		}
	}
	
	public void addGuardedBranch(GuardedNode child){
		if(!hasElse){
			super.addChild(child);
		}else{
			children.add(children.size() - 2, child);
		}
	}
	
	public void addGuardedBranches(GuardedNode... children){
		if(!hasElse){
			super.addChildren(children);
		}else{
			for(AstNode child : children){
				this.children.add(this.children.size() - 2, child);
			}
		}
	}

}
