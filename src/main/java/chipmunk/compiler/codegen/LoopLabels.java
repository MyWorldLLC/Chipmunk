package chipmunk.compiler.codegen;

public class LoopLabels {
	
	protected final String start;
	protected final String guard;
	protected final String end;
	
	public LoopLabels(String start, String guard, String end){
		this.start = start;
		this.guard = guard;
		this.end = end;
	}
	
	public String getStartLabel(){
		return start;
	}
	
	public String getGuardLabel(){
		return guard;
	}
	
	public String getEndLabel(){
		return end;
	}

}
