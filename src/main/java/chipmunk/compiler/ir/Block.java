package chipmunk.compiler.ir;

public abstract class Block {
	
	protected int tokenBeginIndex;
	protected int tokenEndIndex;
	
	public int getTokenBeginIndex(){
		return tokenBeginIndex;
	}
	
	public void setTokenBeginIndex(int index){
		tokenBeginIndex = index;
	}
	
	public int getTokenEndIndex(){
		return tokenEndIndex;
	}
	
	public void setTokenEndIndex(int index){
		tokenEndIndex = index;
	}
}
