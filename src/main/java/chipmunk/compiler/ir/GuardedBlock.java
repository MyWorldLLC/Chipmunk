package chipmunk.compiler.ir;

public class GuardedBlock extends ScopedBlock {
	
	protected ExpressionBlock guard;
	protected Block guarded;
	
	public GuardedBlock(Scope parent){
		super(parent);
	}
	
	public GuardedBlock(){
		super();
	}
	
	public void setGuard(ExpressionBlock guard){
		this.guard = guard;
	}
	
	public ExpressionBlock getGuard(){
		return guard;
	}
	
	public void setGuarded(Block guarded){
		this.guarded = guarded;
	}
	
	public Block getGuarded(){
		return guarded;
	}
}