package chipmunk.compiler.ir;

import java.util.ArrayList;
import java.util.List;

public class GuardedBlock extends ParentBlock {
	
	protected ExpressionBlock guard;
	protected List<Block> guarded;
	
	public GuardedBlock(){
		guarded = new ArrayList<Block>();
	}
	
	public void setGuard(ExpressionBlock guard){
		this.guard = guard;
	}
	
	public ExpressionBlock getGuard(){
		return guard;
	}
	
}