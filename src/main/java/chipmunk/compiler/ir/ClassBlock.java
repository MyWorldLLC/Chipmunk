package chipmunk.compiler.ir;

public class ClassBlock extends ScopedBlock {
	
	protected String name;
	protected String superName;
	
	public ClassBlock(ScopedBlock parent){
		super(parent.getScope());
	}
	
	public ClassBlock(ScopedBlock parent, String name){
		this(parent);
		this.name = name;
	}
	
	public ClassBlock(ScopedBlock parent, String name, String superName){
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
