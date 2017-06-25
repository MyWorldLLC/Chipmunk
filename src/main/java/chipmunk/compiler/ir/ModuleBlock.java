package chipmunk.compiler.ir;

public class ModuleBlock extends ParentBlock {
	
	protected String name;
	
	public ModuleBlock(){
		super();
		scope.setAllowOverrides(true);
		name = "";
	}

	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
}
