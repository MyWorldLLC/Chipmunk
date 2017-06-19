package chipmunk.compiler.ir;

public class VarDecBlock extends ParentBlock {
	
	protected String name;
	
	public VarDecBlock(){
		super();
		name = "";
	}
	
	public VarDecBlock(String name){
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
