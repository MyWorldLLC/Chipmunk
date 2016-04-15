package chipmunk.modules.lang;

public class CMethodType extends CType {

	public CMethodType(){
		super("Method");
	}
	
	public CObject instance(){
		return new CMethod();
	}
	
}
