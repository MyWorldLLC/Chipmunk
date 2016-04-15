package chipmunk.modules.lang;


public class ModuleType extends CType {

	public ModuleType(){
		super("Module");
	}
	
	public CObject instance(){
		return new Module();
	}
	
}
