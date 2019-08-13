package chipmunk.compiler.codegen;

public class CatchBlock extends BlockLabels {

	public int exceptionLocalIndex;
	
	public CatchBlock(String start, String end) {
		super(start, end);
	}
	
}
