package chipmunk.modules.lang;


public class NullType extends CType {

	protected Null nullObject;
	
	public NullType(){
		super("Null");
	}
	
	public CObject instance() {
		// shared instance of null object
		return nullObject;
	}

}
