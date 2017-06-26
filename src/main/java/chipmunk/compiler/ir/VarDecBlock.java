package chipmunk.compiler.ir;

public class VarDecBlock extends ParentBlock {
	
	protected String name;
	protected boolean shared;
	protected boolean isFinal;
	
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

	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}

	public boolean isFinal() {
		return isFinal;
	}

	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

}
