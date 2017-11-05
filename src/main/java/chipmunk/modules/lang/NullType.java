package chipmunk.modules.lang;


public class NullType extends CType {

	public static final Null nullObject = new Null();
	
	public NullType(){
		super("Null");
	}
	
	public CObject instance() {
		// shared instance of null object
		return nullObject;
	}

}
