package chipmunk.compiler.codegen;

public class BlockLabels {

	protected final String start;
	protected final String end;
	
	public BlockLabels(String start, String end) {
		this.start = start;
		this.end = end;
	}
	
	public String getStartLabel() {
		return start;
	}
	
	public String getEndLabel() {
		return end;
	}
}
