package chipmunk.compiler.codegen;

public class LoopLabels extends BlockLabels {
	
	protected final String guard;
	
	public LoopLabels(String start, String guard, String end){
		super(start, end);
		this.guard = guard;
	}
	
	public String getGuardLabel(){
		return guard;
	}
}
