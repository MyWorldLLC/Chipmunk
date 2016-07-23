package chipmunk.compiler.ir;

import java.util.ArrayList;
import java.util.List;

public abstract class Block {
	
	protected List<Block> blocks;
	protected int tokenBeginIndex;
	protected int tokenEndIndex;
	
	public Block(){
		blocks = new ArrayList<Block>();
	}
	
	public List<Block> getBlocks(){
		return blocks;
	}
	
	public void addBlock(Block block){
		blocks.add(block);
	}
	
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
