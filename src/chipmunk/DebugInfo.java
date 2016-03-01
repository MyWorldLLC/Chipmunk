package chipmunk;

public class DebugInfo {

	protected String methodName;
	protected String moduleName;
	protected int lineNumber;
	
	public DebugInfo(){
		methodName = "";
		moduleName = "";
		lineNumber = -1;
	}
	
	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	
}
