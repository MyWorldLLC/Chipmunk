package chipmunk.compiler.ast;

public class TryCatchNode extends AstNode {

	protected boolean hasTry;
	protected boolean hasFinally;
	
	public TryCatchNode(){
		hasTry = false;
		hasFinally = false;
	}
	
	public boolean hasTryBlock(){
		return hasTry;
	}
	
	public void setTryBlock(TryNode tryBlock){
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
		if(hasFinally) {
			children.add(children.size() - 1, catchBlock);
		}else{
			children.add(catchBlock);
		}
	}
	
	public void setFinallyBlock(BlockNode finallyBlock) {
		if(finallyBlock != null){
			if(hasFinally){
				children.remove(children.size() - 1);
			}
			children.add(children.size() - 1, finallyBlock);
			hasFinally = true;
		}else{
			if(hasFinally){
				children.remove(children.size() - 1);
			}
			hasFinally = false;
		}
	}
	
}
