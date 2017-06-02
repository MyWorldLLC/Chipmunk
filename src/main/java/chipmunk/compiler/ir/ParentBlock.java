package chipmunk.compiler.ir;

import java.util.ArrayList;
import java.util.List;

public abstract class ParentBlock extends Block {

	protected List<Block> children;
	
	public ParentBlock(){
		super();
		children = new ArrayList<Block>();
	}
	
	public void addChild(Block child){
		children.add(child);
	}
	
	public List<Block> getChildren(){
		return children;
	}
}
