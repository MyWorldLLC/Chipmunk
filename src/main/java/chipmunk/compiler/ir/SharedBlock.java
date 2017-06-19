package chipmunk.compiler.ir;

public class SharedBlock extends ParentBlock {

	public SharedBlock(){
		super();
	}
	
	public SharedBlock(Block shared){
		super();
		children.add(shared);
	}
	
}
