package chipmunk.compiler.ast;

public class MethodNode extends ScopedNode {

	protected String name;
	protected int defaultParamCount;
	
	public MethodNode(){
		super();
		name = "";
	}
	
	public MethodNode(String name){
		super();
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setDefaultParamCount(int count){
		defaultParamCount = count;
	}
	
	public int getDefaultParamCount(){
		return defaultParamCount;
	}
}
