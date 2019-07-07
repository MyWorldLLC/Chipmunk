package chipmunk.compiler.codegen;

public class IfElseLabels {

	protected final String endLabel;
	
	public IfElseLabels(String end) {
		endLabel = end;
	}
	
	public String getEndLabel() {
		return endLabel;
	}
}
