package chipmunk.compiler.ir;

import java.util.ArrayList;
import java.util.List;

public class ParentBlock extends ScopedBlock {

	protected List<Block> children;
	
	public ParentBlock(Scope outer){
		super(outer);
		children = new ArrayList<Block>();
	}
	
	public ParentBlock(){
		this(null);
	}
	
	public void addChild(Block child){
		children.add(child);
	}
	
	public List<Block> getChildren(){
		return children;
	}
	
}
