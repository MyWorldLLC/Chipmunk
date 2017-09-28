package chipmunk.compiler.ast;

public class ModuleNode extends AstNode {

	protected String name;
	
	public ModuleNode(){
		super();
		name = "";
	}
	
	public ModuleNode(String name){
		super();
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
}
