package chipmunk.compiler.ast;

public class GuardedNode extends ScopedNode {
	
	protected boolean hasGuard;
	
	public GuardedNode(){
		super();
		hasGuard = false;
	}
	
	public GuardedNode(AstNode guard, AstNode... children){
		super(children);
		hasGuard = false;
		setGuard(guard);
	}
	
	public void setGuard(AstNode guard){
		
		if(hasGuard){
			children.remove(0);
			hasGuard = false;
		}
		
		if(guard != null){
			addChildFirst(guard);
			hasGuard = true;
		}
	}
	
	public AstNode getGuard(){
		if(hasGuard){
			return children.get(0);
		}else{
			return null;
		}
	}
	
	public boolean hasGuard(){
		return hasGuard;
	}
	
	public void addChild(AstNode child){
		super.addChild(child);
	}
	
	public void addChildren(AstNode... children){
		super.addChildren(children);
	}

}
