package chipmunk.compiler.ir;

public class ClassBlock extends Block {
	
	protected String name;
	protected String superName;
	
	public ClassBlock(){
		super();
	}
	
	public ClassBlock(String name){
		this();
		this.name = name;
	}
	
	public ClassBlock(String name, String superName){
		this.name = name;
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
