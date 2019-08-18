package chipmunk;

public class CTraceFrame {

	protected String debugSymbol;
	protected int lineNumber;
	
	public CTraceFrame(){
		debugSymbol = "";
		lineNumber = -1;
	}

	public String getDebugSymbol() {
		return debugSymbol;
	}

	public void setDebugSymbol(String debugSymbol) {
		this.debugSymbol = debugSymbol;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	public String toString() {
		return String.format("%s:%d", debugSymbol, lineNumber);
	}
	
}
