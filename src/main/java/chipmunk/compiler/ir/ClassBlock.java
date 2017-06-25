package chipmunk.compiler.ir;

public class ClassBlock extends ParentBlock {
	
	protected String name;
	protected String superName;
	
	public ClassBlock(Scope parent){
		super(parent);
	}
	
	public ClassBlock(Scope parent, String name){
		this(parent);
		this.name = name;
	}
	
	public ClassBlock(Scope parent, String name, String superName){
		this(parent, name);
		this.superName = superName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSuperName() {
		return superName;
	}

	public void setSuperName(String superName) {
		this.superName = superName;
	}

}
