package chipmunk.compiler.ast;

public class ClassNode extends ScopedNode {
	
	protected String name;
	protected String superName;
	
	public ClassNode(){
		super();
		name = "";
	}
	
	public ClassNode(String name){
		super();
		this.name = name;
	}
	
	public ClassNode(String name, String superName){
		super();
		this.name = name;
		this.superName = superName;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getSuperName(){
		return superName;
	}

	public void setSuperName(String superName){
		this.superName = superName;
	}

}
