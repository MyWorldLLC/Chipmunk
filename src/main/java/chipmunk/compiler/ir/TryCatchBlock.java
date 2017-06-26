package chipmunk.compiler.ir;

public class TryCatchBlock extends ParentBlock {
	
	protected ParentBlock catchBlock;

	public TryCatchBlock(){
		super();
		catchBlock = new ParentBlock();
	}
	
	public ParentBlock getCatchBlock(){
		return catchBlock;
	}
}
