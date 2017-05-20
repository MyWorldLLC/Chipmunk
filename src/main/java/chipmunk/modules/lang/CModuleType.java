package chipmunk.modules.lang;


public class CModuleType extends CType {

	public CModuleType(){
		super("Module");
	}
	
	public CObject instance(){
		return new CModule();
	}
	
}
