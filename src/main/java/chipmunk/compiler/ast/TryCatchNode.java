package chipmunk.compiler.ast;

public class TryCatchNode extends AstNode {

	protected boolean hasTry;
	
	public TryCatchNode(){
		hasTry = false;
	}
	
	public boolean hasTryBlock(){
		return hasTry;
	}
	
	public void setTryBlock(BlockNode tryBlock){
		if(tryBlock != null){
			if(hasTry){
				children.remove(0);
			}
			children.add(0, tryBlock);
			hasTry = true;
		}else{
			if(hasTry){
				children.remove(0);
			}
			hasTry = false;
		}
	}
	
	public void addCatchBlock(CatchNode catchBlock){
		children.add(catchBlock);
	}
	
}
