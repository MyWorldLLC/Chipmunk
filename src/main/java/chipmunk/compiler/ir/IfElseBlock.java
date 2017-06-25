package chipmunk.compiler.ir;

import java.util.ArrayList;
import java.util.List;

public class IfElseBlock extends Block {

	protected List<GuardedBlock> ifBlocks;
	protected Block elseBlock;
	
	public IfElseBlock(){
		ifBlocks = new ArrayList<GuardedBlock>();
	}
	
	public List<GuardedBlock> getIfs(){
		return ifBlocks;
	}
	
	public void addIf(GuardedBlock ifBlock){
		ifBlocks.add(ifBlock);
	}
	
	public Block getElseBlock(){
		return elseBlock;
	}
	
	public void setElseBlock(Block elseBlock){
		this.elseBlock = elseBlock;
	}
}
